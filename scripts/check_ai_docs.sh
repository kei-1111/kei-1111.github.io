#!/usr/bin/env bash
# Integrity check for the AI-tooling layout described in ai-docs/README.md.
# Broken symlinks, frontmatter drift, or a single invalidly named Codex agent
# can silently disable skills or agents, so CI runs this on every PR.
# Run from the repository root: ./scripts/check_ai_docs.sh
set -u

repo=$(git rev-parse --show-toplevel 2>/dev/null) || {
  echo "ERROR: not inside a git repository" >&2
  exit 1
}
cd "$repo" || exit 1

# Without nullglob, an empty consumer directory would iterate the literal glob
# string and report a false positive.
shopt -s nullglob

status=0
err() { printf 'ERROR: %s\n' "$1"; status=1; }

# Every canonical skill must be consumed by at least one product side; an
# orphan directory means a rename or removal forgot its symlinks.
for dir in ai-docs/skills/*; do
  [ -d "$dir" ] || continue
  name=$(basename "$dir")
  [ -L ".claude/skills/$name" ] || [ -L ".codex/skills/$name" ] ||
    err "$dir has no consumer symlink in .claude/skills/ or .codex/skills/"
done

# The NavigationRoute template must carry the Metro serializer contribution —
# an older revision without it compiled but silently broke back-stack restore.
nav_template='ai-docs/skills/create-destination/references/templates/NavigationRoute.kt.template'
if [ -f "$nav_template" ]; then
  grep -q '@IntoSet' "$nav_template" ||
    err "$nav_template lacks the @IntoSet SerializersModule contribution (.claude/rules/navigation.md)"
fi

# Consumer-side skill entries are per-skill symlinks into ai-docs/skills/<name>
for link in .claude/skills/* .codex/skills/*; do
  [ -L "$link" ] || { err "$link must be a symlink into ai-docs/skills/<name>"; continue; }
  [ -e "$link" ] || { err "$link is broken (target missing)"; continue; }
  target=$(readlink "$link")
  case "$target" in
    ../../ai-docs/skills/*/*) err "$link points to grouped '$target'; the layout is flat — use ../../ai-docs/skills/<name>" ;;
    ../../ai-docs/skills/*) ;;
    *) err "$link points to '$target', not ../../ai-docs/skills/<name>" ;;
  esac
  [ "$(basename "$link")" = "$(basename "$target")" ] ||
    err "$link name does not match its target directory '$(basename "$target")'"
done

# Every canonical skill / agent procedure holds a SKILL.md with matching frontmatter
for dir in ai-docs/skills/* ai-docs/agents/*; do
  [ -d "$dir" ] || continue
  skill_md="$dir/SKILL.md"
  if [ ! -f "$skill_md" ]; then
    err "$dir has no SKILL.md"
    continue
  fi
done

# Claude agent wrappers reference an existing canonical procedure
for f in .claude/agents/*.md; do
  [ -f "$f" ] || continue
  target=$(grep -o 'ai-docs/agents/[^` ]*/SKILL\.md' "$f" | head -1)
  if [ -z "$target" ]; then
    err "$f does not reference an ai-docs/agents/<name>/SKILL.md"
  elif [ ! -f "$target" ]; then
    err "$f references missing $target"
  fi
done

# Codex agent wrappers: snake_case naming (one invalid name disables ALL custom
# agents) and an existing canonical target
for f in .codex/agents/*.toml; do
  [ -f "$f" ] || continue
  base=$(basename "$f" .toml)
  case "$base" in
    *[!a-z0-9_]*) err "$f: file name must be snake_case ([a-z0-9_])" ;;
  esac
  grep -Eq "^name[[:space:]]*=[[:space:]]*\"$base\"" "$f" || err "$f: 'name' must be \"$base\" (match the file name)"
  target=$(grep -o 'ai-docs/agents/[^" ]*/SKILL\.md' "$f" | head -1)
  if [ -z "$target" ]; then
    err "$f does not reference an ai-docs/agents/<name>/SKILL.md"
  elif [ ! -f "$target" ]; then
    err "$f references missing $target"
  fi
done

# PyYAML keeps frontmatter checks aligned with real YAML semantics.
if ! command -v python3 >/dev/null 2>&1 || ! python3 -c 'import yaml' >/dev/null 2>&1; then
  err "python3 with PyYAML is required for frontmatter validation"
else
  python3 <<'PY' || status=1
import json
import re
import sys
from glob import glob
from pathlib import Path

import yaml


# allowed-tools (tool allowlist) and user-invocable (slash-menu visibility) are
# Claude Code fields; Codex ignores unknown frontmatter keys (verified via
# `codex debug prompt-input` discovery with user-invocable present).
ALLOWED_KEYS = {"name", "description", "allowed-tools", "user-invocable"}
NAME_PATTERN = re.compile(r"[a-z0-9]+(?:-[a-z0-9]+)*")
REFERENCE_PATTERN = re.compile(
    r"\breferences/[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)*"
)
TRIGGER_CASE_KEYS = {"query", "should_trigger"}
failed = False


def error(path, message):
    global failed
    print(f"ERROR: {path}: {message}")
    failed = True


def validate_evals(skill_dir):
    evals_dir = skill_dir / "evals"
    if not evals_dir.is_dir():
        return

    for eval_file in sorted(evals_dir.glob("*.json")):
        try:
            data = json.loads(eval_file.read_text(encoding="utf-8"))
        except (OSError, UnicodeError, json.JSONDecodeError):
            error(eval_file, "eval fixture is not valid JSON")
            continue

        if eval_file.name != "trigger-cases.json":
            continue
        if not isinstance(data, list) or not data:
            error(eval_file, "trigger-cases.json must be a non-empty JSON array")
            continue

        for index, case in enumerate(data, start=1):
            if not isinstance(case, dict) or set(case) != TRIGGER_CASE_KEYS:
                error(
                    eval_file,
                    f"entry {index} must be an object with exactly the keys "
                    "query and should_trigger",
                )
                continue
            if not isinstance(case["query"], str) or not case["query"].strip():
                error(eval_file, f"entry {index} query must be a non-empty string")
            if type(case["should_trigger"]) is not bool:
                error(eval_file, f"entry {index} should_trigger must be a boolean")


for skill_file_name in sorted(
    glob("ai-docs/skills/*/SKILL.md")
    + glob("ai-docs/agents/*/SKILL.md")
):
    skill_file = Path(skill_file_name)
    skill_dir = skill_file.parent
    text = skill_file.read_text(encoding="utf-8")
    lines = text.splitlines()

    if len(lines) > 500:
        error(skill_file, "SKILL.md exceeds 500 lines")

    closing_index = None
    if lines and lines[0] == "---":
        try:
            closing_index = lines.index("---", 1)
        except ValueError:
            pass

    if closing_index is None:
        error(skill_file, "no frontmatter block")
        validate_evals(skill_dir)
        continue

    frontmatter_text = "\n".join(lines[1:closing_index])
    try:
        frontmatter = yaml.safe_load(frontmatter_text)
    except yaml.YAMLError:
        frontmatter = None

    if not isinstance(frontmatter, dict):
        error(skill_file, "frontmatter is not valid YAML")
    else:
        for key in sorted(frontmatter, key=lambda value: str(value)):
            if key not in ALLOWED_KEYS:
                error(skill_file, f"frontmatter has unknown key {key!r}")

        name = frontmatter.get("name")
        if not isinstance(name, str):
            error(skill_file, "frontmatter name must be a string")
        else:
            if name != skill_dir.name:
                error(
                    skill_file,
                    f"frontmatter name {name!r} != directory name {skill_dir.name!r}",
                )
            if NAME_PATTERN.fullmatch(name) is None:
                error(
                    skill_file,
                    "frontmatter name must use lowercase kebab-case",
                )
            if len(name) > 64:
                error(skill_file, "frontmatter name exceeds 64 characters")

        description = frontmatter.get("description")
        if not isinstance(description, str):
            error(skill_file, "frontmatter description must be a string")
        else:
            if not description.strip():
                error(skill_file, "frontmatter description must not be empty")
            if len(description) > 1024:
                error(
                    skill_file,
                    "frontmatter description exceeds 1024 characters",
                )

    body = "\n".join(lines[closing_index + 1 :])
    for reference in sorted(set(REFERENCE_PATTERN.findall(body))):
        if not (skill_dir / reference).exists():
            error(skill_file, f"references missing file {reference}")

    validate_evals(skill_dir)

# Rule frontmatter drives path-scoped loading and list_matching_rules.sh; a
# malformed block silently turns a scoped rule into dead weight.
for rule_file_name in sorted(glob(".claude/rules/*.md")):
    rule_file = Path(rule_file_name)
    rule_lines = rule_file.read_text(encoding="utf-8").splitlines()
    if not rule_lines or rule_lines[0] != "---":
        continue
    try:
        rule_closing = rule_lines.index("---", 1)
    except ValueError:
        error(rule_file, "unterminated frontmatter block")
        continue
    try:
        rule_fm = yaml.safe_load("\n".join(rule_lines[1:rule_closing]))
    except yaml.YAMLError:
        error(rule_file, "frontmatter is not valid YAML")
        continue
    if not isinstance(rule_fm, dict) or set(rule_fm) != {"paths"}:
        error(rule_file, "rule frontmatter must contain exactly the key 'paths'")
        continue
    rule_paths = rule_fm["paths"]
    if (
        not isinstance(rule_paths, list)
        or not rule_paths
        or not all(isinstance(p, str) and p.strip() for p in rule_paths)
    ):
        error(rule_file, "'paths' must be a non-empty list of non-empty strings")

# The plan/report templates are one design family; diverging CSS means one was
# edited alone.
TEMPLATE_DIR = Path("ai-docs/skills/implement-issue/references")
plan = TEMPLATE_DIR / "plan-template.html"
report = TEMPLATE_DIR / "report-template.html"
if plan.is_file() and report.is_file():

    def style_block(path):
        text = path.read_text(encoding="utf-8")
        try:
            return text[text.index("<style>") : text.index("</style>")]
        except ValueError:
            error(path, "no <style> block")
            return None

    plan_css, report_css = style_block(plan), style_block(report)
    if plan_css is not None and report_css is not None and plan_css != report_css:
        error(report, "template CSS differs from plan-template.html (one design family)")

sys.exit(1 if failed else 0)
PY
fi

if [ "$status" -ne 0 ]; then
  echo 'ai-docs structure check FAILED'
fi
[ "$status" -eq 0 ] && echo "ai-docs structure check passed"
exit "$status"

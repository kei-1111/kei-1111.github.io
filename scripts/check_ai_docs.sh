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

# Root AGENTS.md must name every tree that carries a nested AGENTS.md — the
# enumeration silently drifted when shared/AGENTS.md was added.
for nested in */AGENTS.md; do
  [ -f "$nested" ] || continue
  tree=$(dirname "$nested")
  grep -q "\`$tree/\`" AGENTS.md ||
    err "AGENTS.md does not mention \`$tree/\` although $nested exists"
done

# Golden comments must be PLACEHOLDER markers — anything else survives into
# instantiated destinations and violates the comment policy.
while IFS= read -r golden; do
  [ -f "$golden" ] || continue
  bad=$(grep -nE '^[[:space:]]*//' "$golden" | grep -v 'PLACEHOLDER' || true)
  [ -z "$bad" ] || err "$golden has non-PLACEHOLDER comment(s): $(printf '%s' "$bad" | head -1)"
done < <(git ls-files 'template/src/*.kt' 'template/src/**/*.kt')

for checklist in ai-docs/skills/create-destination/references/checklists/*.md; do
  grep -Eq '```|alias\(libs\.plugins|fun NavBackStack|updateViewModelState|MviEffect\(|entry<[^>]*Name' "$checklist" &&
    err "$checklist repeats implementation syntax instead of completion outcomes"
done

for rule in .claude/rules/preview.md .claude/rules/mvi-architecture.md .claude/rules/mvi-testing.md; do
  grep -Eq '^```k(otlin|t)?$' "$rule" && err "$rule copies Kotlin source instead of pointing to it"
done

grep -q 'DEV_CORS_HOSTS' ai-docs/skills/verify-app/SKILL.md &&
  err "verify-app must not prescribe DEV_CORS_HOSTS while the client base URL is fixed"

grep -Fq 'ApiConfigTest.kt' .claude/hooks/pre-push-api-config.sh ||
  err "pre-push-api-config must compare API_BASE_URL with the production pin in ApiConfigTest"
source .claude/hooks/pre-push-api-config.sh
commented_config='// internal const val API_BASE_URL = "https://commented.example"
internal const val API_BASE_URL = "https://production.example"'
parsed_api_base_url=$(printf '%s\n' "$commented_config" |
  parse_kotlin_string_constant API_BASE_URL)
[ "$parsed_api_base_url" = 'https://production.example' ] ||
  err "pre-push-api-config treats a commented API_BASE_URL as the active declaration"
correlated_assertions='private const val PRODUCTION_API_BASE_URL = "https://production.example"
assertEquals("https://unrelated.example", someValue) // API_BASE_URL is tested below'
parsed_pin=$(printf '%s\n' "$correlated_assertions" |
  parse_kotlin_string_constant PRODUCTION_API_BASE_URL)
[ "$parsed_pin" = 'https://production.example' ] ||
  err "pre-push-api-config correlates the production pin with an unrelated assertion"
literal_paren_pin='private const val PRODUCTION_API_BASE_URL = "https://production.example/path)"'
parsed_pin=$(printf '%s\n' "$literal_paren_pin" |
  parse_kotlin_string_constant PRODUCTION_API_BASE_URL)
[ "$parsed_pin" = 'https://production.example/path)' ] ||
  err "pre-push-api-config cannot parse a literal closing parenthesis in the pinned URL"
if verify_api_config_pin 'https://wrong.example' 'https://production.example' >/dev/null 2>&1; then
  err "pre-push-api-config accepts a URL that differs from the production pin"
fi
api_config_path='app/core/api/src/commonMain/kotlin/io/github/kei_1111/app/core/api/network/ApiConfig.kt'
api_config_test_path='app/core/api/src/commonTest/kotlin/io/github/kei_1111/app/core/api/network/ApiConfigTest.kt'
worktree_api_base_url=$(parse_kotlin_string_constant API_BASE_URL < "$api_config_path")
worktree_pinned_api_base_url=$(
  parse_kotlin_string_constant PRODUCTION_API_BASE_URL < "$api_config_test_path"
)
if ! verify_api_config_pin "$worktree_api_base_url" "$worktree_pinned_api_base_url" >/dev/null 2>&1; then
  err "pre-push-api-config rejects the API_BASE_URL pinned in the working tree"
fi
if git diff --quiet HEAD -- "$api_config_path" "$api_config_test_path"; then
  if ! printf '%s' '{"tool_input":{"command":"git push"}}' |
    CLAUDE_PROJECT_DIR="$repo" bash .claude/hooks/pre-push-api-config.sh >/dev/null 2>&1; then
    err "pre-push-api-config rejects the API_BASE_URL currently pinned in HEAD"
  fi
fi

for target in \
  app/webApp/src/commonMain/kotlin/io/github/kei_1111/app/App.kt \
  app/webApp/src/wasmJsMain/kotlin/io/github/kei_1111/app/Main.kt; do
  scripts/list_matching_rules.sh "$target" | grep -Fq '.claude/rules/error-handling.md:' ||
    err ".claude/rules/error-handling.md does not apply to $target"
done

scripts/list_matching_rules.sh \
  server/src/main/kotlin/io/github/kei_1111/server/client/PublishedContent.kt |
  grep -Fq '.claude/rules/naming-conventions.md:' ||
  err ".claude/rules/naming-conventions.md does not apply to server Kotlin"

shared_validation=$(grep -F '| `shared:model` models or serializers |' .claude/rules/working-agreement.md)
for task in ':shared:model:jvmTest' ':shared:model:wasmJsTest' ':server:test'; do
  case "$shared_validation" in
    *"$task"*) ;;
    *) err "shared:model validation is missing $task" ;;
  esac
done

# The golden NavigationRoutes must carry the Metro serializer contribution —
# an older revision without it compiled but silently broke back-stack restore.
for nav_golden in \
  template/src/commonMain/kotlin/io/github/kei_1111/template/navigation/TemplateScreenNavigationRoute.kt \
  template/src/commonMain/kotlin/io/github/kei_1111/template/navigation/TemplateDialogNavigationRoute.kt; do
  if [ -f "$nav_golden" ]; then
    grep -q '@IntoSet' "$nav_golden" ||
      err "$nav_golden lacks the @IntoSet SerializersModule contribution (.claude/rules/navigation.md)"
  else
    err "$nav_golden is missing"
  fi
done

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
import runpy
import sys
import tempfile
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


extractor_path = Path(
    "ai-docs/skills/audit-ai-docs/references/extract_session_digests.py"
)
extractor = runpy.run_path(str(extractor_path))
with tempfile.NamedTemporaryFile("w", encoding="utf-8") as session_log:
    for timestamp, body in (
        ("2026-08-08T02:08:10Z", "before cutoff"),
        ("2026-08-08T02:08:12Z", "after cutoff"),
    ):
        session_log.write(
            json.dumps(
                {
                    "type": "user",
                    "timestamp": timestamp,
                    "message": {"content": body},
                }
            )
            + "\n"
        )
    session_log.flush()
    cutoff = extractor["parse_ts"]("2026-08-08T02:08:11Z")
    digest = extractor["digest_lines"](session_log.name, cutoff)
    if digest != ["[2026-08-08T02:08] USER: after cutoff"]:
        error(extractor_path, "digest extraction includes evidence outside the cutoff")


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

    if isinstance(frontmatter, dict) and "allowed-tools" in frontmatter:
        allowed = str(frontmatter["allowed-tools"])
        bash_prefixes = re.findall(r"Bash\(([^:)]+):?\*?\)", allowed)
        in_bash = False
        for line in body.splitlines():
            stripped = line.strip()
            if stripped.startswith("```"):
                in_bash = stripped == "```bash"
                continue
            if not in_bash or not stripped or stripped.startswith("#"):
                continue
            first = stripped.split()[0]
            if first.startswith("<") or first.startswith("$"):
                continue
            if not any(first.startswith(p) for p in bash_prefixes):
                error(
                    skill_file,
                    f"bash block runs {first!r} but allowed-tools grants only "
                    f"{bash_prefixes}",
                )

    validate_evals(skill_dir)

consumed_agents = set()
for wrapper in sorted(Path(".claude/agents").glob("*.md")):
    lines = wrapper.read_text(encoding="utf-8").splitlines()
    closing_index = None
    if lines and lines[0] == "---":
        try:
            closing_index = lines.index("---", 1)
        except ValueError:
            pass
    if closing_index is None:
        error(wrapper, "no frontmatter block")
        continue
    try:
        frontmatter = yaml.safe_load("\n".join(lines[1:closing_index]))
    except yaml.YAMLError:
        frontmatter = None
    if not isinstance(frontmatter, dict):
        error(wrapper, "frontmatter is not valid YAML")
        continue
    if frontmatter.get("name") != wrapper.stem:
        error(wrapper, f"frontmatter name must match file name {wrapper.stem!r}")

    targets = re.findall(r"ai-docs/agents/([^/]+)/SKILL\.md", "\n".join(lines))
    if len(targets) != 1:
        error(wrapper, "must reference exactly one canonical agent procedure")
        continue
    target = targets[0]
    if target != wrapper.stem:
        error(wrapper, f"targets {target!r}, which does not match wrapper name {wrapper.stem!r}")
    consumed_agents.add(target)

for wrapper in sorted(Path(".codex/agents").glob("*.toml")):
    text = wrapper.read_text(encoding="utf-8")
    name_match = re.search(r'^name\s*=\s*"([^"]+)"\s*$', text, re.MULTILINE)
    if name_match is None or name_match.group(1) != wrapper.stem:
        error(wrapper, f"name must match file name {wrapper.stem!r}")

    targets = re.findall(
        r"ai-docs/agents/([^/]+)/SKILL\.md",
        text,
    )
    if len(targets) != 1:
        error(wrapper, "must reference exactly one canonical agent procedure")
        continue
    target = targets[0]
    expected_wrapper = target.replace("-", "_")
    if expected_wrapper != wrapper.stem:
        error(
            wrapper,
            f"targets {target!r}, which maps to wrapper name {expected_wrapper!r}",
        )
    consumed_agents.add(target)

canonical_agents = {
    path.parent.name for path in Path("ai-docs/agents").glob("*/SKILL.md")
}
for orphan in sorted(canonical_agents - consumed_agents):
    error(Path("ai-docs/agents") / orphan, "has no Claude or Codex agent wrapper")

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

catalog_text = Path("gradle/libs.versions.toml").read_text(encoding="utf-8")
versions_block = catalog_text.split("[versions]", 1)[1].split("\n[", 1)[0]
catalog_versions = set(re.findall(r'^\w+\s*=\s*"([^"]+)"', versions_block, re.MULTILINE))
guidance_files = (
    [Path("AGENTS.md"), Path("CLAUDE.md")]
    + sorted(Path(".").glob("*/AGENTS.md"))
    + sorted(Path(".claude/rules").glob("*.md"))
    + sorted(Path("ai-docs").rglob("*.md"))
    + sorted(Path("docs").glob("*Overview*.md"))
)
single_source_facts = {
    "./gradlew :app:webApp:wasmJsBrowserDistribution": Path(".claude/rules/gradle.md"),
    "./gradlew :app:webApp:wasmJsBrowserDevelopmentRun": Path(".claude/rules/gradle.md"),
    "./gradlew :server:run": Path(".claude/rules/gradle.md"),
    "./gradlew :server:buildFatJar": Path(".claude/rules/gradle.md"),
}
for guidance_file in guidance_files:
    guidance = guidance_file.read_text(encoding="utf-8")
    for fact, canonical_file in single_source_facts.items():
        if fact in guidance and guidance_file != canonical_file:
            error(
                guidance_file,
                f"copies {fact!r}; canonical source is {canonical_file}",
            )
    for version in sorted(catalog_versions):
        if version in guidance:
            error(
                guidance_file,
                f"copies catalog version {version!r}; point to gradle/libs.versions.toml",
            )

# AGENTS.md files are entrypoints, not second implementation guides. Detect
# copied prose without maintaining a brittle list of implementation identifiers.
canonical_rule_text = " ".join(
    " ".join(path.read_text(encoding="utf-8").split())
    for path in sorted(Path(".claude/rules").glob("*.md"))
)
token_pattern = re.compile(r"[A-Za-z0-9@:.#/_-]+")
for agents_file in [Path("AGENTS.md")] + sorted(Path(".").glob("*/AGENTS.md")):
    for line_number, line in enumerate(
        agents_file.read_text(encoding="utf-8").splitlines(), 1
    ):
        tokens = token_pattern.findall(line)
        if len(tokens) < 10:
            continue
        for start in range(len(tokens) - 9):
            phrase = " ".join(tokens[start : start + 10])
            if phrase in canonical_rule_text:
                error(
                    agents_file,
                    f"line {line_number} copies canonical rule prose ({phrase!r}); point to the rule",
                )
                break

# "`<file>.md` — <Heading>" pointers must resolve to a real heading in the
# target; a section rename silently strands every reference to it. Lowercase
# tails are prose, not heading references, and are left alone.
heading_ref_pattern = re.compile(r"`([A-Za-z0-9._/-]+\.md)` — ([^`]+)")
heading_cache = {}


def heading_resolves(part, headings):
    return any(
        heading == part or heading.startswith(part + " ") for heading in headings
    )
for guidance_file in guidance_files:
    flat_text = " ".join(guidance_file.read_text(encoding="utf-8").split())
    for ref_path, ref_tail in heading_ref_pattern.findall(flat_text):
        target = Path(ref_path)
        if not target.is_file():
            target = Path(".claude/rules") / Path(ref_path).name
            if not target.is_file():
                continue
        if target not in heading_cache:
            heading_cache[target] = {
                heading_line.lstrip("#").strip()
                for heading_line in target.read_text(encoding="utf-8").splitlines()
                if heading_line.startswith("#")
            }
        candidate = re.split(r"[.,;:()|]", ref_tail, 1)[0]
        candidate = re.sub(r"\s+in full$", "", candidate.strip()).strip()
        for part in candidate.split(" / "):
            words = part.split()
            while words and not words[-1][0].isalpha():
                words.pop()
            part = " ".join(words)
            # A heading may itself contain " and ", so only fall back to
            # splitting a compound pointer when the whole part resolves nothing.
            if not part or heading_resolves(part, heading_cache[target]):
                continue
            for sub_part in part.split(" and "):
                sub_words = sub_part.split()
                if not sub_words or heading_resolves(sub_part, heading_cache[target]):
                    continue
                if len(sub_words) >= 2 and all(
                    word[0].isupper() for word in sub_words
                ):
                    error(
                        guidance_file,
                        f"references heading {sub_part!r} missing from {target}",
                    )

# gradle.md documents the same commands CI executes; assert each workflow still
# runs what the doc claims so independent hardcodes cannot drift apart.
gradle_rule = Path(".claude/rules/gradle.md")
gradle_rule_text = gradle_rule.read_text(encoding="utf-8")
for command, workflow in {
    ":app:webApp:wasmJsBrowserDistribution": Path(".github/workflows/deploy-app.yml"),
    ":server:buildFatJar": Path(".github/workflows/deploy-server.yml"),
}.items():
    if command not in gradle_rule_text:
        error(gradle_rule, f"no longer documents {command!r}")
    if command not in workflow.read_text(encoding="utf-8"):
        error(workflow, f"does not run {command!r} documented in {gradle_rule}")

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

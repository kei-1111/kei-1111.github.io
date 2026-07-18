#!/usr/bin/env bash
# Integrity check for the AI-tooling layout described in ai-docs/README.md.
# Broken symlinks, frontmatter drift, or a single invalidly named Codex agent
# can silently disable skills or agents, so CI runs this on every PR.
# Run from the repository root: ./scripts/check_ai_docs.sh
set -u
# Without nullglob, an empty consumer directory would iterate the literal glob
# string and report a false positive.
shopt -s nullglob

fail=0
err() { printf 'ERROR: %s\n' "$1"; fail=1; }

# Consumer-side skill entries are per-skill symlinks into ai-docs/skills/<group>/<name>
for link in .claude/skills/* .codex/skills/*; do
  [ -L "$link" ] || { err "$link must be a symlink into ai-docs/skills/<group>/<name>"; continue; }
  [ -e "$link" ] || { err "$link is broken (target missing)"; continue; }
  target=$(readlink "$link")
  case "$target" in
    ../../ai-docs/skills/*/*) ;;
    *) err "$link points to '$target', not ../../ai-docs/skills/<group>/<name>" ;;
  esac
  [ "$(basename "$link")" = "$(basename "$target")" ] ||
    err "$link name does not match its target directory '$(basename "$target")'"
done

# Every canonical skill / agent procedure holds a SKILL.md with matching frontmatter
for dir in ai-docs/skills/*/* ai-docs/agents/*/*; do
  [ -d "$dir" ] || continue
  name=$(basename "$dir")
  skill_md="$dir/SKILL.md"
  if [ ! -f "$skill_md" ]; then
    err "$dir has no SKILL.md"
    continue
  fi
  fm_name=$(sed -n 's/^name:[[:space:]]*//p' "$skill_md" | head -1)
  [ "$fm_name" = "$name" ] || err "$skill_md frontmatter name '$fm_name' != directory name '$name'"
  grep -q '^description:' "$skill_md" || err "$skill_md has no description in its frontmatter"
done

# Claude agent wrappers reference an existing canonical procedure
for f in .claude/agents/*.md; do
  [ -f "$f" ] || continue
  target=$(grep -o 'ai-docs/agents/[^` ]*/SKILL\.md' "$f" | head -1)
  if [ -z "$target" ]; then
    err "$f does not reference an ai-docs/agents/<group>/<name>/SKILL.md"
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
    err "$f does not reference an ai-docs/agents/<group>/<name>/SKILL.md"
  elif [ ! -f "$target" ]; then
    err "$f references missing $target"
  fi
done

if [ "$fail" -ne 0 ]; then
  echo 'ai-docs structure check FAILED'
  exit 1
fi
echo 'ai-docs structure check passed'

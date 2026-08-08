#!/usr/bin/env bash
# Scaffolds a new skill: canonical directory under ai-docs/skills/<name>/,
# SKILL.md skeleton, consumer symlinks, then the structure check. Which sides
# get a symlink decides which product uses the skill (ai-docs/README.md).
# Usage: scripts/new_skill.sh <kebab-case-name> [--claude-only|--codex-only] [--internal]
#   --internal  adds user-invocable: false (agent-invoked only, hidden from the / menu)
set -u

die() { printf 'ERROR: %s\n' "$1" >&2; exit 1; }

cd "$(git rev-parse --show-toplevel)" || exit 1

name='' sides=both internal=0
for arg in "$@"; do
  case "$arg" in
    --claude-only) [ "$sides" = both ] || die '--claude-only and --codex-only are mutually exclusive'; sides=claude ;;
    --codex-only) [ "$sides" = both ] || die '--claude-only and --codex-only are mutually exclusive'; sides=codex ;;
    --internal) internal=1 ;;
    --*) die "unknown option: $arg" ;;
    *) [ -z "$name" ] || die 'exactly one skill name expected'; name=$arg ;;
  esac
done
[ -n "$name" ] || { echo 'usage: scripts/new_skill.sh <kebab-case-name> [--claude-only|--codex-only] [--internal]' >&2; exit 2; }
printf '%s' "$name" | grep -Eq '^[a-z0-9]+(-[a-z0-9]+)*$' || die "name must be lowercase kebab-case: $name"
[ "${#name}" -le 64 ] || die "name exceeds 64 characters: $name"
[ ! -e "ai-docs/skills/$name" ] || die "ai-docs/skills/$name already exists"

mkdir -p "ai-docs/skills/$name" || die 'mkdir failed'
{
  echo '---'
  echo "name: $name"
  echo 'description: TODO — what this skill does + when to use it, with concrete trigger phrases (Japanese included).'
  [ "$internal" -eq 1 ] && echo 'user-invocable: false'
  echo '---'
  echo
  echo "# $name"
  echo
  echo 'TODO'
} > "ai-docs/skills/$name/SKILL.md" || die 'writing SKILL.md failed'

if [ "$sides" != codex ]; then
  ln -s "../../ai-docs/skills/$name" ".claude/skills/$name" || die 'creating .claude/skills symlink failed'
fi
if [ "$sides" != claude ]; then
  ln -s "../../ai-docs/skills/$name" ".codex/skills/$name" || die 'creating .codex/skills symlink failed'
fi

./scripts/check_ai_docs.sh || die 'structure check failed after scaffolding'

echo "scaffolded ai-docs/skills/$name (sides: $sides, internal: $internal)"
echo 'next: write the description (what + when + triggers) and body, then verify discovery'
echo '  Claude: the skill appears in the / menu (unless --internal)'
echo '  Codex:  codex debug prompt-input "hi" lists it under ## Skills'

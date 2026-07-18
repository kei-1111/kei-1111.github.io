---
name: implement-issue
description: Implement a GitHub Issue end to end on the current branch, from reading the issue to a validated working-tree change. Use when the user asks to work on, 対応する, or implement a given Issue number.
---

# Implement issue

## Task overview

Take a GitHub Issue from number to a validated working-tree change on the current branch.
Committing and the PR are separate steps (`create-commit` / `create-pr`).

## Branch precondition

The user manages branches (`<type>/#<issue-number>`). Confirm the current branch matches the
target Issue; on mismatch, stop and ask — never create branches or worktrees yourself.

## Workflow

1. **Fetch the issue** — `gh issue view <N>` for the title, body, and type
2. **Investigate impact** — locate the affected modules/files and every usage of what will change;
   read the nearest analogous implementation
3. **Read conventions** — the project guide and the docs applicable to the touched areas
4. **Plan** — settle target files, approach, and validation before editing; if the Issue leaves
   any room for interpretation or the change is large, present the plan (asking where unsure) and
   wait for the user's approval
5. **Implement** — delegate execution to the `implementer` subagent with the concrete plan
   (contract: `ai-docs/agents/implementation/implementer/SKILL.md`), then review the diff yourself
6. **Validate** — run the narrowest relevant validation (e.g. `./gradlew :app:feature:<name>:compileKotlinWasmJs`,
   `./gradlew detekt` — rerun once if autoCorrect reformats)
7. **Report** — changed files, validation results, and any deviation from the Issue with its reason

## Notes

- Make the smallest coherent change; if the Issue bundles several concerns, propose splitting first
- If investigation contradicts the Issue's premise, report instead of improvising

## Argument handling

| Argument | Behavior |
|---|---|
| Issue number / URL | Target that Issue |
| (none) | Derive `#<N>` from the current branch name `<type>/#<N>` |

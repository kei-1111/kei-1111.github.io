---
name: implement-issue
description: Implement a GitHub Issue end to end on the current branch, from reading the issue to a validated and reviewed working-tree change. Use when the user asks to work on, 対応する, or implement a given Issue number without shipping intent — for the full Issue-to-PR flow use ship-issue instead.
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
7. **Cross-review loop** — run the product's independent review lane over the working-tree change,
   up to 3 rounds; a round with no actionable findings ends the loop early. Per round: fix clear
   violations (rule violations, divergence from the Issue, bugs) immediately and re-validate; ask
   the user before acting on judgment calls (design decisions, scope changes); record rejected
   findings with their verification result. If findings have not converged after 3 rounds, stop
   and consult the user
8. **Report** — changed files, validation results, review rounds with fixed/rejected findings,
   and any deviation from the Issue with its reason. When the user invoked this skill directly,
   also render the report as an HTML page (Claude Code: publish it as an Artifact; a product
   without artifact publishing writes the HTML file and reports its path); when running as an
   inner step of another skill, skip the HTML — the outermost report owns it

## Notes

- Make the smallest coherent change; if the Issue bundles several concerns, propose splitting first
- If investigation contradicts the Issue's premise, report instead of improvising

## Argument handling

| Argument | Behavior |
|---|---|
| Issue number / URL | Target that Issue |
| `no-review` | Skip the Cross-review loop step |
| (none) | Derive `#<N>` from the current branch name `<type>/#<N>` |

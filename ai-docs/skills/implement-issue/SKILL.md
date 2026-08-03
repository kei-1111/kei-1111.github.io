---
name: implement-issue
description: Internal step of the ship-issue chain — implement a GitHub Issue on the current branch, from reading the issue to a validated working-tree change with review depth scaled to the change size. Invoked by the agent from ship-issue; the user-facing entry point is ship-issue, and this skill fires directly only when the user explicitly asks for an implementation-only run with no PR.
user-invocable: false
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
4. **Plan** — settle target files, approach, validation, and the change size (see below) before
   editing; if the Issue leaves any room for interpretation or the change is Large, present the
   plan (asking where unsure) — for a Large change, presented as an HTML page rendered from
   `references/plan-template.html` per that template's own header contract (Claude Code:
   publish it as an Artifact; a product without artifact publishing writes the HTML file and
   reports its path) — and wait for the user's approval
5. **Implement** — delegate execution to the product's default implementation lane with the
   concrete plan (contract: `ai-docs/agents/implementer/SKILL.md`; on Claude Code the default
   lane is the `codex-implementer` subagent per `CLAUDE.md` — Model routing, judgment-heavy
   edits staying on `implementer`), then review the diff yourself;
   a Small change may instead be edited directly without delegation. When the change adds or
   modifies logic in a testable layer, run this step through the `tdd` skill's red-green-refactor
   workflow instead of implementing first and testing after
6. **Validate** — run the narrowest relevant validation (e.g. `./gradlew :app:feature:<name>:compileKotlinWasmJs`,
   `./gradlew detekt` — rerun once if autoCorrect reformats)
7. **Review** — depth per the change size:
   - **Small**: run only the rules reviewer of the independent review lane (cheap, diff-scoped —
     a single-file edit can still be rule-dense); handle its findings as in Medium
   - **Medium**: one round of the independent review lane; fix clear violations (rule violations,
     divergence from the Issue, bugs, added comments the comment policy does not admit)
     immediately and re-validate; record rejected findings with their verification result
   - **Large**: full cross-review loop — up to 3 rounds; round 1 runs the independent review lane
     and, where the product has one, the cross-model reviewer in parallel on the same diff (keep
     the lanes independent). When the change was implemented through the Codex lane, the
     Claude-side independent lane is the cross-model check — a Codex review of Codex-implemented
     code is a separate-session self-review, not an independent one; weigh it accordingly. Later
     rounds re-run the independent review lane alone to confirm the
     fixes; a round with no actionable findings ends the loop early. Per round, handle findings as
     in Medium, and ask the user before acting on judgment calls (design decisions, scope
     changes). If findings have not converged after 3 rounds, stop and consult the user
8. **Report** — as text: open with a prose overview of what was changed and why, then changed
   files, validation results, review rounds with fixed/rejected findings, and any deviation
   from the Issue with its reason. The HTML report belongs to the outermost `ship-issue`
   report (`references/report-template.html`) — this step never renders one

## Change size

Classify during Plan; when in doubt, pick the larger tier. The user's explicit override always wins.

| Size | Criteria |
|---|---|
| Small | The diff is explainable in one sentence — single file or equivalently narrow, no cross-module or wiring impact |
| Medium | Multi-file change contained in one module/feature, established patterns, low risk |
| Large | Cross-module, DI/navigation/build wiring, release-impacting, or the Issue leaves real ambiguity |

## Notes

- Make the smallest coherent change; if the Issue bundles several concerns, propose splitting first
- If investigation contradicts the Issue's premise, report instead of improvising
- `references/plan-template.html` and `references/report-template.html` are one design family
  sharing the same CSS tokens (also used by `ship-issue` via a `references` symlink) — edit the
  two files together, never one alone

## Argument handling

| Argument | Behavior |
|---|---|
| Issue number / URL | Target that Issue |
| `small` / `medium` / `large` | Override the change-size classification |
| `no-review` | Skip the Review step regardless of size |
| (none) | Derive `#<N>` from the current branch name `<type>/#<N>` |

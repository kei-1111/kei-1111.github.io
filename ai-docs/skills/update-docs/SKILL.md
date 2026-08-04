---
name: update-docs
description: Internal step of the ship-issue chain — update project documents made stale by the current branch's change before the PR is created. Invoked by the agent from ship-issue, not by the user.
user-invocable: false
---

# Update docs

## Task overview

Find every project document the current change has made stale and bring it up to date with the
smallest natural edit. Run after the code change is complete, before creating the PR.

## Document surfaces

| Document | Check when |
|---|---|
| `AGENTS.md` | Conventions, architecture, or workflows it describes changed |
| `CLAUDE.md` | Its project summary or top-level guidance drifted |
| `.claude/rules/*.md` | A convention in the touched area changed |
| `docs/ArchitectureOverview.md` / `docs/ModuleOverview.md` | Architecture or module structure changed |
| `README.md` | The user-facing project description changed |
| `ai-docs/README.md` | The AI asset layout or sharing rules changed |
| `ai-docs/skills/**` / `ai-docs/agents/**` | A procedure they document changed |

## Workflow

1. **Scope the change** — review `git fetch origin main` then `git diff origin/main...HEAD` (and the working tree) for what actually changed; the local `main` ref may be stale
2. **Collect candidates** — search the surfaces above for the changed symbols, paths, and concepts
3. **Verify against code** — current source is authoritative; touch only statements that are now wrong
4. **Edit minimally** — fix the existing sentence in place rather than adding new sections
5. **Re-read** — read each edited document start to finish and confirm it reads naturally as a whole
6. **Report** — list updated docs and checked-but-current docs; "no updates needed" is a valid outcome

## Writing constraints

- Docs describe the current state — never narrate the change, its history, or its rationale
- Keep each edit proportional: one clear sentence over repeated wording or speculative safeguards
- Do not let rule documents grow when the change only warrants adjusting a line

## Argument handling

| Argument | Behavior |
|---|---|
| Free-form context | Treat as a hint about where the change's impact lies |
| (none) | Derive the scope from the branch diff |

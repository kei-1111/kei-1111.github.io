# Git Workflow

Core Git/GitHub facts for kei-1111.github.io, loaded in every session. Step-by-step
procedures live in the create-commit / create-issue / create-pr / triage-pr-reviews skills.

## Commits

- [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/), written entirely in English: `<type>: <description>` or `<type>(scope): <description>`
- Types: feat, fix, docs, refactor, perf, test, build, ci, chore — `test` applies to the `:server` test suite (`server/src/test/`), the client unit tests (`app/**/src/commonTest/`), the `shared/model` commonTest suite, the Playwright E2E suite (`:test:e2e`), the Konsist conventions suite (`:test:conventions`), or the custom detekt rule tests (`detekt-rules/src/test/`)
- Observed scopes: `profile`, `splash`, `core`, `designsystem`, `app`, `utils`, `deps`, `server`, `shared`, `e2e`
- Description: imperative mood, one concise line, no trailing period
- Breaking changes: `feat!:` or a `BREAKING CHANGE:` footer
- Granularity: one self-contained logical change per commit, cherry-pickable without depending on later commits

Examples: `fix(profile): allow horizontal scrolling in TerminalPanel`, `feat(shared): add Work model to the client/server JSON contract`

## Branches

- Name: `<type>/#<issue-number>` where type mirrors the Issue type: `feature` | `fix` (`[Bug]`) | `refactor` | `docs` | `research` | `perf` | `test` | `ci` | `chore` (e.g. `feature/#18`, `fix/#20`, `chore/#43`)
- Keep branches short-lived and synced with `main`

## Issues

- Title `[<Type>]: <title>`; title and body in English, mirroring the headings of the matching template in `.github/ISSUE_TEMPLATE/`
- One responsibility per Issue; close when completed
- Filing many findings at once (audits, review sweeps): batch them into per-kind checklist Issues — never one Issue per finding
- One Issue maps to one PR — never split an Issue's items across PRs; when a change feels PR-splittable, split the Issue instead, and keep Issue bodies free of "separate PRs" phrasing
- When the implementation deviates from the Issue's stated approach, update the Issue body to match
- An Issue made obsolete by consolidation is deleted (`gh issue delete`), not closed — closing means completed; also remove references to it from the absorbing Issue

## Pull Requests

- Title: the corresponding Issue title verbatim; base branch is always `main`
- Body follows `.github/PULL_REQUEST_TEMPLATE.md` (canonical — its inline comments state which optional sections apply)
- Keep PRs reviewable (up to ~500 lines) and don't repeat information already in the Issue or diff
- Merge method: a merge commit — never switch to squash/rebase without an explicit user instruction

## CI/CD

Canonical detail: `.claude/rules/ci-cd.md` and the workflow files in `.github/workflows/`.

- `PreToolUse` hooks (`.claude/hooks/pre-push-*.sh`, wired in `.claude/settings.json`) gate
  `git push` commands: detekt must pass cleanly, and `ApiConfig.kt` at HEAD must match the
  production origin pinned by `ApiConfigTest`. Each hook source owns its exact detection and
  command behavior.

## Prohibited

- Direct push to the `main` branch
- Force push on shared branches
- Massive file changes in a single PR
- Meaningless commit messages

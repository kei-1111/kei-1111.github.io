# Git Workflow

Core Git/GitHub facts for kei-1111.github.io, loaded in every session. Step-by-step
procedures live in the create-commit / create-issue / create-pr / triage-pr-reviews skills.

## Commits

- [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/), written entirely in English: `<type>: <description>` or `<type>(scope): <description>`
- Types: feat, fix, docs, refactor, perf, test, build, ci, chore — `test` applies to the `:server` test suite (`server/src/test/`), the client unit tests (`app/**/src/commonTest/`), the `shared/model` commonTest suite, or the Playwright E2E suite (`:test:e2e`)
- Observed scopes: `profile`, `splash`, `core`, `designsystem`, `app`, `utils`, `deps`, `server`, `shared`
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

## Pull Requests

- Title: the corresponding Issue title verbatim; base branch is always `main`
- Body follows `.github/PULL_REQUEST_TEMPLATE.md`: `## Summary` / `## Related Issue` / `## Checklist` always; `## Cause and Fix` for bug fixes only; `## UI Changes` (Before/After image table) for UI changes only
- Keep PRs reviewable (up to ~500 lines) and don't repeat information already in the Issue or diff

## CI/CD

Canonical detail: `.claude/rules/ci-cd.md` (path-scoped to `.github/**` — the 10 workflow files, CD pipelines, Playwright cache warming, docs-only gate). In short: every PR to `main` runs 3 always-on script checks plus detekt / compile / test workflows that skip for docs-only changes; merging a PR deploys immediately.

- A `PreToolUse` hook (`.claude/hooks/pre-push-detekt.sh`, wired in `.claude/settings.json`) runs `./gradlew detekt` before a `git push` (substring match on the hook payload) and blocks the push unless it passes cleanly — an autoCorrect reformat also blocks, since the committed code would still fail CI; commit the formatting fix and push again.

## Prohibited

- Direct push to the `main` branch
- Force push on shared branches
- Massive file changes in a single PR
- Meaningless commit messages

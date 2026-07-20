# Git Workflow

Core Git/GitHub facts for kei-1111.github.io, loaded in every session. Step-by-step
procedures live in the create-commit / create-issue / create-pr / triage-pr-reviews skills.

## Commits

- [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/), written entirely in English: `<type>: <description>` or `<type>(scope): <description>`
- Types: feat, fix, docs, refactor, perf, test, build, ci, chore — `test` applies to the `:server` test suite (`server/src/test/`), the only place tests live in this repo
- Observed scopes: `profile`, `splash`, `core`, `designsystem`, `app`, `utils`, `deps`, `server`, `shared`
- Description: imperative mood, one concise line, no trailing period
- Breaking changes: `feat!:` or a `BREAKING CHANGE:` footer
- Granularity: one self-contained logical change per commit, cherry-pickable without depending on later commits

Examples: `feat(profile): allow horizontal scrolling in ProjectTree`, `chore(designsystem): remove unused color and duration tokens`

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

- CI (`.github/workflows/ci.yml`): every PR to `main` runs the script checks (`./scripts/check_ai_docs.sh` for AI-tooling structure, `./scripts/check_destination_isolation.sh`, `./scripts/check_gradle_conventions.sh`) and `./gradlew detekt :app:webApp:compileKotlinWasmJs compileAndroidMain :server:test` (JDK 21, temurin; autoCorrect disabled on CI). Run `./gradlew detekt` locally before pushing — local autoCorrect may reformat on the first run, so re-run until it passes.
- CD (`.github/workflows/cd-app.yml` + `cd-server.yml`): push to `main` builds `:app:webApp:wasmJsBrowserDistribution` and deploys it to GitHub Pages, skipping server-only changes (`paths-ignore`); pushes touching `server/**`/`shared/**`/build config build `:server:buildFatJar` and deploy it to Cloud Run via Workload Identity Federation. Merging a PR deploys immediately — a PR must build and pass detekt before merge.

## Prohibited

- Direct push to the `main` branch
- Force push on shared branches
- Massive file changes in a single PR
- Meaningless commit messages

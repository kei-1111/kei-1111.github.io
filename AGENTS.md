# AGENTS.md

This file gives coding agents project-specific guidance for working in this repository. It applies to the whole repository; nested `AGENTS.md` files in `app/`, `server/`, and `test/` add per-tree rules, and the closest file to the code being changed takes precedence.

## Instruction Priority

Canonical detail: `.claude/rules/working-agreement.md` — Instruction Priority. In short: the user's current request, then the closest applicable agent guidance, then current source code, then the overview docs; source code wins over drifted documentation.

## Project Overview

kei-1111.github.io is a portfolio web application for kei-1111 (basic info, works, skills, SNS links), also serving as a Compose Multiplatform (CMP) learning project. Deployed at https://kei-1111.github.io/ via GitHub Pages.

The UI mimics the Android Studio IDE New UI with switchable Islands Dark and Light themes and a switchable Japanese/English display language: a project tree, a code editor showing the profile as Kotlin source with syntax highlighting, and a Compose Preview pane rendering the actual profile card. Editor code and Preview content must stay in sync.

Tech stack:

- Kotlin / Compose Multiplatform
- **wasmJs** — the only distribution target for the client (browser, GitHub Pages)
- **Android** — exists for exactly two roles: rendering commonMain `@Preview` in the IDE (layoutlib) and running the client unit tests — UseCase and ViewModel — on the local JVM (host tests, `.claude/rules/app-testing.md` / `mvi-testing.md`). Never shipped, no Android runtime features
- **Ktor server** (`server/`, JVM) — serves `/api/profile`, `/api/contributions`, and `/api/issues` backed by the GitHub GraphQL API, deployed to Cloud Run
- Multimodule Clean Architecture + MVI using `MviViewModel<ViewModelState, State, Intent>`
- Metro DI (`@ContributesBinding` / `@SingleIn` / `@Inject`), `metrox-viewmodel` (`metroViewModel()`)
- Navigation 3 (`androidx.navigation3`), a single `NavDisplay` + `NavKey` back stack
- kotlinx.serialization
- detekt (autoCorrect enabled locally, disabled on CI)
- Custom `KeiTheme` design system (Islands Dark/Light colors, typography, shapes, and icons); the active theme is hoisted state owned by `app:webApp`'s `App` and passed as `KeiTheme(isDark)`. `MaterialTheme` is NOT used

## Read First

Use these documents as the source of truth:

- `docs/ArchitectureOverview.md` — data flow, DI, navigation (Japanese)
- `docs/ModuleOverview.md` — module dependency graph and per-module responsibilities (Japanese)
- `ai-docs/README.md` — how AI-tooling assets are laid out and shared between Claude Code and Codex
- `.claude/rules/*.md` — per-area conventions; the canonical homes this file's rule sections summarize and point to

Workflow skills automate common flows and are auto-discovered by each tool (Claude Code from `.claude/skills/`, Codex from `.codex/skills/`) — no skill list is maintained in this file. Workflow skills are canonical in `ai-docs/skills/<group>/<name>/` and agent procedures in `ai-docs/agents/<group>/<name>/`; each tool's directory holds flat per-skill symlinks for the skills it uses. See `ai-docs/README.md` for the layout and sharing rules.

Only reference rule or workflow files that currently exist in this repository. Do not assume untracked workflow skills, tests, or Android runtime infrastructure are available.

## Working Agreement

Canonical detail: `.claude/rules/working-agreement.md` — the before-editing, while-editing, and handing-off rules, the comment policy (comments are exceptional — default none; only individually justified constraints the code cannot express, living in the file they describe), and the scope-of-a-request rules (filing an issue or asking an opinion never starts implementation; no commit/push/Issue/PR unless asked).

## Module Roles

Four top-level trees plus build logic: `app/` (wasm client — `app/webApp` DI/navigation root, `app/core/*`, `app/feature/*`), `server/` (Ktor CIO API on Cloud Run), `shared/model/` (data classes shared by client and server), `test/` (`test/tags` testTag constants + `test/e2e` Playwright suite), `build-logic/` (the six `kei_1111.*` convention plugins). Full per-module responsibilities and the dependency graph: `docs/ModuleOverview.md` (canonical home); per-tree rules: the nested `AGENTS.md` in each tree.

## Build And Validation

Canonical detail: `.claude/rules/working-agreement.md` — Build And Validation (the change→minimum-validation table, dev-server/detekt/test-suite notes). In short: prefer the narrowest command that covers the change, and never claim browser behavior was verified from compilation alone.

## Git And PR Rules

Canonical detail: `.claude/rules/git-workflow.md` (commit/branch/Issue/PR formats, CI/CD workflows, docs-only gate).

- Commit messages: Conventional Commits in concise imperative English; branch names `<type>/#<issue-number>`; Issue and PR titles/bodies in English following the repository templates.
- Run `./gradlew detekt` before pushing — autoCorrect may reformat on the first run; commit the reformat and rerun until it passes cleanly. (Claude Code enforces this automatically via a pre-push hook.)
- Do not push directly to `main`; do not force-push a shared branch unless the user explicitly requests it and the impact is understood.
- Do not commit, push, create an Issue, or open a PR unless the user asks (canonical: `.claude/rules/working-agreement.md` — Scope Of A Request).
- A PR must build and pass detekt before merge; docs-only changes skip the heavy CI jobs (details: `.claude/rules/git-workflow.md` — CI/CD).

## Dependency Updates

Follow the full policy in `.claude/rules/gradle.md` — Dependency Updates (canonical home):
version-catalog-only bumps, Kotlin as the anchor for coupled versions, one upgrade per
branch/PR, and the validation command.

- MUST: declare every dependency with `implementation()`; `api()` is prohibited so a build file states exactly what its module depends on (`scripts/check_gradle_conventions.sh`).

## Safety And Maintenance

Canonical detail: `.claude/rules/working-agreement.md` — Safety And Maintenance (secrets, the Android two-roles stub constraint, version-catalog and convention-plugin rules, no heavy dependencies or large rewrites without need, source wins over drifted docs). Keep this file and that rule updated together when agent-level instructions change.

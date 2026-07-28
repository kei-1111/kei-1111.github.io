# AGENTS.md

This file gives coding agents project-specific guidance for working in this repository. It applies to the whole repository; nested `AGENTS.md` files in `app/`, `server/`, and `test/` add per-tree rules, and the closest file to the code being changed takes precedence.

## Instruction Priority

When guidance conflicts, use this order:

1. The user's current request
2. The closest applicable `AGENTS.md`
3. Current source code and build configuration
4. `docs/ArchitectureOverview.md` and `docs/ModuleOverview.md`

Treat source code as authoritative when generated documentation, examples, or copied patterns have drifted. Preserve this project's established targets, navigation structure, previews, dispatchers, resources, and validation approach.

## Project Overview

kei-1111.github.io is a portfolio web application for kei-1111 (basic info, works, skills, SNS links), also serving as a Compose Multiplatform (CMP) learning project. Deployed at https://kei-1111.github.io/ via GitHub Pages.

The UI mimics the Android Studio IDE New UI with switchable Islands Dark and Light themes and a switchable Japanese/English display language: a project tree, a code editor showing the profile as Kotlin source with syntax highlighting, and a Compose Preview pane rendering the actual profile card. Editor code and Preview content must stay in sync.

Tech stack:

- Kotlin / Compose Multiplatform
- **wasmJs** — the only distribution target for the client (browser, GitHub Pages)
- **Android** — exists ONLY so the IDE can render commonMain `@Preview` (layoutlib). Never shipped, no Android runtime features
- **Ktor server** (`server/`, JVM) — serves `/api/profile` and `/api/contributions` backed by the GitHub GraphQL API, deployed to Cloud Run
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

Before editing:

- Inspect the files being changed and their nearest analogous implementation.
- Check `git status`; preserve user changes and avoid unrelated cleanup.
- Verify referenced APIs, tasks, modules, and paths in the current checkout instead of relying on documentation alone.
- For a non-trivial change, define verifiable success criteria first — the narrowest validation that must pass and, for user-visible UI changes, what to confirm in the browser — and validate against them before reporting completion.

While editing:

- Make the smallest coherent change that satisfies the request.
- Follow existing module boundaries and naming before introducing a new abstraction.
- Keep refactors separate from behavior changes unless the refactor is required.
- Do not edit generated files or build output.
- Do NOT write self-evident comments. Comment only non-obvious constraints or rationale (why, not what).
- Keep documentation concise and proportional; prefer one clear instruction over repeated wording, exhaustive safeguards, or speculative edge cases.
- Escalate when stuck: after a few failed attempts without a confirmed root cause, stop and consult the user instead of applying speculative fixes.

Before handing off:

- Review the final diff for accidental or unrelated changes.
- Verify before asserting: check API existence and behavior against the resolved dependency version or official sources; confirm the running build actually contains the change before diagnosing from runtime observations; distinguish live data from fallbacks before declaring end-to-end success; separate observation from speculation when reporting.
- When a skill step names the independent review lane, it maps to the `rules_reviewer` and `code_reviewer` agents run independently; a cross-model reviewer exists only on Claude Code (the `codex-review` skill — see `CLAUDE.md`).
- Run the narrowest relevant validation, expanding to broader checks for cross-module or release-impacting changes.
- Report what changed, what was validated, and anything not validated.

## Module Roles

Four top-level trees plus build logic: `app/` (wasm client — `app/webApp` DI/navigation root, `app/core/*`, `app/feature/*`), `server/` (Ktor CIO API on Cloud Run), `shared/model/` (data classes shared by client and server), `test/` (`test/tags` testTag constants + `test/e2e` Playwright suite), `build-logic/` (the six `kei_1111.*` convention plugins). Full per-module responsibilities and the dependency graph: `docs/ModuleOverview.md` (canonical home); per-tree rules: the nested `AGENTS.md` in each tree.

## Build And Validation

Prefer the narrowest command that covers the change. Suggested validation by change type:

| Change | Minimum validation |
|---|---|
| Kotlin in one feature | `./gradlew :app:feature:<name>:compileKotlinWasmJs` |
| Compose UI or Preview | Feature wasm compile + `./gradlew :app:feature:<name>:compileAndroidMain` |
| Core module or cross-module API | Compile every directly affected consumer |
| Navigation, DI, Gradle, or app wiring | `./gradlew :app:webApp:wasmJsBrowserDistribution` |
| Server Kotlin | `./gradlew :server:test` (compiles and runs the server test suite) |
| Formatting or lint-sensitive Kotlin | `./gradlew detekt`; rerun if auto-correct changed files |
| User-visible wasm UI | Production build and, when practical, the browser smoke test (`.claude/rules/ui-implementation.md`) |
| E2E test infra (`test/tags`, `test/e2e`) | `./gradlew :test:e2e:compileTestKotlin`; to actually run it, serve `:app:webApp:wasmJsBrowserDistribution`'s output and `./gradlew :test:e2e:test -PbaseUrl=...` |

Full command list (dev server, production build, server run, E2E): `.claude/rules/gradle.md` — Build Commands (canonical home).

Important:

- The `:app:webApp:` prefix on the dev-server task is required — an unqualified `wasmJsBrowserDevelopmentRun` can start a different module's dev server on the same port.
- detekt: autoCorrect quirks (a reformat can fail the first run — rerun it; never fix import ordering manually) and key rules: `.claude/rules/gradle.md` — detekt (canonical home).
- Test suites: `:server:test` (JUnit 5 + Ktor `testApplication` + `MockEngine`; CI runs it) per `.claude/rules/server-testing.md`; `:test:e2e` (Playwright against a served build, gated on `-PbaseUrl`; CI runs it via `ui-test.yml`) per `.claude/rules/ui-testing.md`. The client modules (`app/*`, `shared/*`) themselves have no tests.
- Do not claim browser behavior was verified when only compilation or static analysis was run; the browser smoke test procedure is `.claude/rules/ui-implementation.md` — Browser Smoke Test (canonical home).

## Git And PR Rules

Canonical detail: `.claude/rules/git-workflow.md` (commit/branch/Issue/PR formats, CI/CD workflows, docs-only gate).

- Commit messages: Conventional Commits in concise imperative English; branch names `<type>/#<issue-number>`; Issue and PR titles/bodies in English following the repository templates.
- Run `./gradlew detekt` before pushing — autoCorrect may reformat on the first run; commit the reformat and rerun until it passes cleanly. (Claude Code enforces this automatically via a pre-push hook.)
- Do not push directly to `main`; do not force-push a shared branch unless the user explicitly requests it and the impact is understood.
- Do not commit, push, create an Issue, or open a PR unless the user asks for that action.
- A PR must build and pass detekt before merge; docs-only changes skip the heavy CI jobs (details: `.claude/rules/git-workflow.md` — CI/CD).

## Dependency Updates

Follow the full policy in `.claude/rules/gradle.md` — Dependency Updates (canonical home):
version-catalog-only bumps, Kotlin as the anchor for coupled versions, one upgrade per
branch/PR, and the validation command.

- MUST: declare every dependency with `implementation()`; `api()` is prohibited so a build file states exactly what its module depends on (`scripts/check_gradle_conventions.sh`).

## Safety And Maintenance

- Never expose secrets, credentials, tokens, signing material, or machine-specific configuration.
- The Android target is preview-only: androidMain actuals may be no-op (`openUrl`, `fetchText` returning `null`, etc.) — never add Android-specific runtime features or network calls there.
- Declare all dependencies in `gradle/libs.versions.toml` and reference them via the version catalog, including inside convention plugins (`libs.findLibrary(...)`). Do NOT use the deprecated `compose.dependencies.*` Gradle accessors — specify artifacts directly.
- Prefer the existing convention plugins (`kei_1111.detekt`, `kei_1111.kmp.wasm`, `kei_1111.cmp`, `kei_1111.kmp.feature`, `kei_1111.kmp.shared`, `kei_1111.metro`) over ad hoc Gradle configuration.
- Do not add heavy dependencies without approval.
- Do not rewrite large areas, rename public APIs, or move code across modules unless the task requires it.
- Never discard or overwrite unrelated working-tree changes.
- When generated templates or docs disagree with current source code, the source wins.
- Keep this file updated when agent-level instructions change.

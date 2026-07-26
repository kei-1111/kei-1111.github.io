# AGENTS.md

This file gives coding agents project-specific guidance for working in this repository. It applies to the whole repository. If a nested `AGENTS.md` is added later, the closest file to the code being changed takes precedence.

## Instruction Priority

When guidance conflicts, use this order:

1. The user's current request
2. The closest applicable `AGENTS.md`
3. Current source code and build configuration
4. `docs/ArchitectureOverview.md` and `docs/ModuleOverview.md`

Treat source code as authoritative when generated documentation, examples, or copied patterns have drifted. Preserve this project's established targets, navigation structure, previews, dispatchers, resources, and validation approach.

## Project Overview

kei-1111.github.io is a portfolio web application for kei-1111 (basic info, works, skills, SNS links), also serving as a Compose Multiplatform (CMP) learning project. Deployed at https://kei-1111.github.io/ via GitHub Pages.

The UI mimics the Android Studio IDE New UI with switchable Islands Dark and Light themes: a project tree, a code editor showing the profile as Kotlin source with syntax highlighting, and a Compose Preview pane rendering the actual profile card. Editor code and Preview content must stay in sync.

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

Four top-level trees: `app/` (wasm client), `server/` (Ktor API), `shared/` (models shared by both), `test/` (Playwright E2E).

- `app/webApp/` — Entry point. `AppGraph` (Metro `@DependencyGraph` DI root) and `AppNavDisplay` (single Navigation 3 `NavDisplay` + `NavKey` back stack, wires `splashEntries()` / `profileEntries()`). wasmJs only — no Android target
- `app/core/mvi/` — MVI base: `MviViewModel<VS, S, I>`, the `Intent` / `State` / `ViewModelState<S>` marker interfaces, and the `MviEffect` composable (consumes a one-shot Effect and auto-fires `ConsumeEffect`)
- `app/core/navigation/` — Navigation 3 dependencies and shared transitions, plus the Composition Local-based `ResultEventBus` / `ResultEffect` bridge for one-shot destination results
- `app/core/domain/` — UseCases (`GetProfileUseCase`, `GetContributionsUseCase`, `GetLicensesUseCase`): thin `internal class` wrappers around a single Repository call, each bound via `@ContributesBinding(AppScope::class)`
- `app/core/data/` — Repositories: `ProfileRepository` and `ContributionsRepository` both fetch from the project's own API (`API_BASE_URL` in `network/ApiConfig.kt`) and fall back to static snapshots (`FallbackProfile` / `FallbackContributions`) when the fetch fails or on the preview-only Android target; `ThemeRepository` persists the theme choice (`saveIsDark`); `LicensesRepository` emits the static third-party license content (`LicenseContent`) via `flowOf`
- `app/core/common/` — `Result<T>` (Loading/Success/Error) + `Flow<T>.asResult()`, the `DefaultDispatcher` qualifier and its `DispatcherBindings` Metro `@BindingContainer`, and the app-scoped `InteractionLog` (`logging/`: records visitor interactions as Logcat-style entries, timestamps via expect/actual)
- `app/core/designsystem/` — `KeiTheme(isDark)` resolving and distributing the Dark/Light `KeiColorScheme` (which carries `isDark`), `KeiTypography`, `KeiShapes`, and `KeiIcons`; theme switching is a callback threaded from `app:webApp`. Also owns fonts (JetBrains Mono + Noto Sans JP + Zen Kaku Gothic New), the responsive `WindowLayout` / `windowLayoutFor(width)`, and `LinkServiceStyle` (a `LinkServiceType`'s icon and brand colour). Everything here answers "what does this look like"
- `app/core/ui/` — Stateful Compose helpers with no visual identity (`HoverState`). Anything that fixes a colour, shape, or dimension — and every shared composable — belongs in `designsystem` instead
- `app/core/utils/` — `openUrl` expect/actual (wasmJs: `window.open`, android: no-op), plus `rememberIsPageVisible` / `prefersReducedMotion`, the `VerticalResizeCursor` / `HorizontalResizeCursor` pointer icons (wasmJs: `PointerIcon.fromKeyword`, android: default cursor), and `visitorDeviceLabel` (wasmJs: browser + OS from the User-Agent)
- `app/feature/profile/` — Main IDE-style portfolio screen (tree / editor / preview pane / Logcat tool window / status bar)
- `app/feature/splash/` — Build-log-style splash screen shown while fonts preload
- `shared/model/` — Data classes shared by client and server: `GitHubProfile` / `PinnedRepo` / `LanguageShare` / `LinkService`, `ContributionCalendar` / `ContributionDay` (KMP: wasmJs + preview Android + jvm targets via `kei_1111.kmp.shared`)
- `server/` — Ktor (CIO) JVM server. `GET /api/profile` (static profile from `ProfileContent.kt` merged with live GitHub stats) and `GET /api/contributions`, both backed by the GitHub GraphQL API with a TTL cache; deployed to Cloud Run
- `test/tags/` — `TestTags` constants (e.g. `TestTags.Profile.TITLE_BAR_THEME_TOGGLE`) shared between `Modifier.testTag(...)` calls in `app/feature/*` and Playwright locators in `test/e2e/`; KMP (`kei_1111.kmp.shared`: wasmJs + jvm + preview Android), wired into every feature module's commonMain via `KmpFeaturePlugin`
- `test/e2e/` — Playwright (JVM) + JUnit 5 tests driving a built `:app:webApp:wasmJsBrowserDistribution` in a real browser. `PlaywrightTestBase` owns the browser/page lifecycle; `page/SplashPage.kt` is a Page Object for the Splash→Profile wait. Run via `./gradlew :test:e2e:test -PbaseUrl=...` — the task is `onlyIf` the property is set, so it never runs as part of `check`/`build`; not wired into CI yet
- `build-logic/` — Convention plugins: `kei_1111.detekt`, `kei_1111.kmp.wasm`, `kei_1111.cmp`, `kei_1111.kmp.feature`, `kei_1111.kmp.shared`, `kei_1111.metro`

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
| User-visible wasm UI | Production build and, when practical, the browser smoke test below |
| E2E test infra (`test/tags`, `test/e2e`) | `./gradlew :test:e2e:compileTestKotlin`; to actually run it, serve `:app:webApp:wasmJsBrowserDistribution`'s output and `./gradlew :test:e2e:test -PbaseUrl=...` |

Full command list (dev server, production build, server run, E2E): `.claude/rules/gradle.md` — Build Commands (canonical home).

Important:

- The `:app:webApp:` prefix on the dev-server task is required — an unqualified `wasmJsBrowserDevelopmentRun` can start a different module's dev server on the same port.
- detekt: autoCorrect quirks (a reformat can fail the first run — rerun it; never fix import ordering manually) and key rules: `.claude/rules/gradle.md` — detekt (canonical home).
- `:server` has unit/integration tests (`server/src/test/`, JUnit 5 + kotlin.test, Ktor `testApplication` + `MockEngine`); run with `./gradlew :server:test` (CI runs this). `:test:e2e` has Playwright/JUnit 5 browser tests against a built distribution; run with `./gradlew :test:e2e:test -PbaseUrl=...` (not wired into CI yet). They cover client UI behavior only — no server-connectivity verification. Test conventions live per suite: `.claude/rules/server-testing.md` and `.claude/rules/ui-testing.md` (canonical homes; a future `mvi-testing.md` covers the planned ViewModel unit tests). The client modules (`app/*`, `shared/*`) themselves have no tests.
- Do not claim browser behavior was verified when only compilation or static analysis was run.

Browser smoke test (user-visible wasm UI changes): follow the procedure in
`.claude/rules/ui-implementation.md` — Browser Smoke Test (canonical home; Playwright-first, manual
dev server as fallback). Report which checks were performed and call out anything left unverified.

## Architecture Rules

Canonical detail: `.claude/rules/mvi-architecture.md` (MVI types, ViewModel pattern, `onIntent` policy, Effect handling) and `.claude/rules/ui-implementation.md` (screen layering, directory layout).

- Layering: `app:feature` → `app:core:domain` → `app:core:data`; a feature module has no Gradle dependency on `app:core:data` — a ViewModel only ever calls a UseCase, never a Repository (canonical: `.claude/rules/data-layer.md`).
- Screen structure: `XxxScreenRoot` → internal `XxxScreen` (branches on the `900.dp` breakpoint) → `content/` Desktop/Mobile Content → pure `component/*` (plain values + callbacks, never an `Intent`).
- MVI flow: `Intent` → `onIntent` updates `ViewModelState` → `toState()` derives `State`; one-shot side effects live as `State.effect` and are consumed exactly once via `MviEffect`, so every `XxxIntent` includes `ConsumeEffect`.

## Data, Domain, And Error Handling

Canonical detail: `.claude/rules/data-layer.md` (Repository shape, fetch & fallback), `.claude/rules/error-handling.md` (`Result<T>` boundary), `.claude/rules/usecase.md` (UseCase shape and Metro bindings).

- There is NO `Dispatchers.IO` on wasm — never introduce an `@IoDispatcher`; use the `DefaultDispatcher` qualifier from `app/core/common`.
- Fetch/parse failures fall back to static snapshots (`FallbackProfile` / `FallbackContributions`) by design — do not convert this to error propagation.
- Profile source content lives in the server's `ProfileContent.kt` (`DefaultGitHubProfile`), with a client copy in `app/core/data`'s `FallbackProfile` — update both together.

## Navigation Rules

Canonical detail: `.claude/rules/navigation.md` (route/entries file layout, dialog destinations, `ResultEventBus` results).

- Navigation 3: a single `NavDisplay` + back stack owned by `webApp`'s `AppNavDisplay`; cross-feature navigation is a plain lambda parameter on `xxxEntries()` — a feature never depends on another feature module.
- CRITICAL: register every new `NavKey` in `AppNavDisplay`'s `navKeySavedStateConfiguration` `SerializersModule` — wasmJs has no reflection, so a missing registration compiles fine but silently breaks back-stack save/restore.
- Dialogs and command palettes are dialog destinations on the back stack (`DialogSceneStrategy`), not ad-hoc UI state.

## Compose UI Rules

Canonical detail: `.claude/rules/ui-implementation.md` (theme/color usage, per-surface selection colors, IDE design rules, Compose pitfalls) and `.claude/rules/preview.md` (Preview conventions).

- Never hardcode a new color — add a field to `KeiColorScheme`. Selection colors copy the real Android Studio surface by surface; `androidGreen` is content-side only, never a chrome selection state.
- The editor code pane (left) and the Preview pane (right) must always show the same data — update both together.

## Naming Rules

Canonical detail: `.claude/rules/naming-conventions.md` (Intent/Effect patterns, callbacks, UseCases, packages, testTag, text content).

- Every destination defines the 5-file MVI set plus `XxxScreenRoot` / `XxxScreen` (dialogs: `XxxDialogRoot` / `XxxDialog`) at the `destination/<name>/` top level; Content in `content/`, local models in `model/`, UI tokens in `theme/`.
- MUST: a destination never references a sibling destination — especially not its components (`scripts/check_destination_isolation.sh` enforces it). Promotion rules: `.claude/rules/ui-implementation.md`.
- Intent names are intent-based (`UpdateLayout`, `ToggleTree`), never operation-based (`OnSaveButtonClick` is prohibited).
- No `strings.xml` — UI text is static Kotlin content; Japanese literals are allowed in content data and composables.

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

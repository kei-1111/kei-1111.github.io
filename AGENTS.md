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
- **wasmJs** — the only distribution target (browser, GitHub Pages)
- **Android** — exists ONLY so the IDE can render commonMain `@Preview` (layoutlib). Never shipped, no Android runtime features
- Multimodule Clean Architecture + MVI using `MviViewModel<ViewModelState, State, Intent>`
- Metro DI (`@ContributesBinding` / `@SingleIn` / `@Inject`), `metrox-viewmodel` (`metroViewModel()`)
- Navigation 3 (`androidx.navigation3`), a single `NavDisplay` + `NavKey` back stack
- kotlinx.serialization
- detekt (autoCorrect enabled locally, disabled on CI)
- Custom `KeiTheme` design system (Islands Dark/Light colors, typography, shapes, and icons), switched through `KeiThemeController`. `MaterialTheme` is NOT used

## Read First

Use these documents as the source of truth:

- `docs/ArchitectureOverview.md` — data flow, DI, navigation (Japanese)
- `docs/ModuleOverview.md` — module dependency graph and per-module responsibilities (Japanese)
- `ai-docs/README.md` — how AI-tooling assets are laid out and shared between Claude Code and Codex

Workflow skills automate common flows and are auto-discovered by each tool (Claude Code from `.claude/skills/`, Codex from `.codex/skills/`) — no skill list is maintained in this file. Workflow skills are canonical in `ai-docs/skills/<group>/<name>/` and agent procedures in `ai-docs/agents/<group>/<name>/`; each tool's directory holds flat per-skill symlinks for the skills it uses. See `ai-docs/README.md` for the layout and sharing rules.

Only reference rule or workflow files that currently exist in this repository. Do not assume untracked workflow skills, tests, or Android runtime infrastructure are available.

## Working Agreement

Before editing:

- Inspect the files being changed and their nearest analogous implementation.
- Check `git status`; preserve user changes and avoid unrelated cleanup.
- Verify referenced APIs, tasks, modules, and paths in the current checkout instead of relying on documentation alone.

While editing:

- Make the smallest coherent change that satisfies the request.
- Follow existing module boundaries and naming before introducing a new abstraction.
- Keep refactors separate from behavior changes unless the refactor is required.
- Do not edit generated files or build output.
- Do NOT write self-evident comments. Comment only non-obvious constraints or rationale (why, not what).
- Keep documentation concise and proportional; prefer one clear instruction over repeated wording, exhaustive safeguards, or speculative edge cases.

Before handing off:

- Review the final diff for accidental or unrelated changes.
- Run the narrowest relevant validation, expanding to broader checks for cross-module or release-impacting changes.
- Report what changed, what was validated, and anything not validated.

## Module Roles

- `composeApp/` — Entry point. `AppGraph` (Metro `@DependencyGraph` DI root) and `AppNavDisplay` (single Navigation 3 `NavDisplay` + `NavKey` back stack, wires `splashEntries()` / `profileEntries()`). wasmJs only — no Android target
- `core/mvi/` — MVI base: `MviViewModel<VS, S, I>`, the `Intent` / `State` / `ViewModelState<S>` marker interfaces, and the `MviEffect` composable (consumes a one-shot Effect and auto-fires `ConsumeEffect`)
- `core/domain/` — UseCases (`GetProfileUseCase`, `GetContributionsUseCase`): thin `internal class` wrappers around a single Repository call, each bound via `@ContributesBinding(AppScope::class)`
- `core/data/` — Repositories: `ProfileRepository` (static `GitHubProfile` content, `ProfileContent.kt`), `ContributionsRepository` (fetches the GitHub contribution calendar, falls back to the static `FallbackContributions` snapshot when the fetch fails or on the preview-only Android target)
- `core/model/` — Data classes: `GitHubProfile` / `PinnedRepo` / `LanguageShare` / `LinkService`, `ContributionCalendar` / `ContributionDay`
- `core/common/` — `Result<T>` (Loading/Success/Error) + `Flow<T>.asResult()`, the `DefaultDispatcher` qualifier and its `DispatcherBindings` Metro `@BindingContainer`
- `core/designsystem/` — `KeiTheme` distributing the active Dark/Light `KeiColorScheme`, `KeiTypography`, `KeiShapes`, and `KeiIcons`; `KeiThemeController` switches themes. Also owns fonts (JetBrains Mono + Noto Sans JP + Zen Kaku Gothic New) and the responsive `WindowLayout` / `windowLayoutFor(width)`
- `core/utils/` — `openUrl` expect/actual (wasmJs: `window.open`, android: no-op), plus `rememberIsPageVisible` / `prefersReducedMotion` expect/actual
- `feature/profile/` — Main IDE-style portfolio screen (tree / editor / preview pane / status bar)
- `feature/splash/` — Build-log-style splash screen shown while fonts preload
- `build-logic/` — Convention plugins: `kei_1111.detekt`, `kei_1111.kmp.wasm`, `kei_1111.cmp`, `kei_1111.kmp.feature`, `kei_1111.metro`

## Build And Validation

Prefer the narrowest command that covers the change. Suggested validation by change type:

| Change | Minimum validation |
|---|---|
| Kotlin in one feature | `./gradlew :feature:<name>:compileKotlinWasmJs` |
| Compose UI or Preview | Feature wasm compile + `./gradlew :feature:<name>:compileAndroidMain` |
| Core module or cross-module API | Compile every directly affected consumer |
| Navigation, DI, Gradle, or app wiring | `./gradlew :composeApp:wasmJsBrowserDistribution` |
| Formatting or lint-sensitive Kotlin | `./gradlew detekt`; rerun if auto-correct changed files |
| User-visible wasm UI | Production build and, when practical, browser smoke test |

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # Dev server (http://localhost:8080) — the :composeApp: prefix is required
./gradlew :composeApp:wasmJsBrowserDistribution    # Production build (used by CD)
./gradlew detekt                                   # Lint (autoCorrect enabled locally)
./gradlew :feature:profile:compileKotlinWasmJs     # Compile a single module (wasm)
./gradlew :feature:profile:compileAndroidMain      # Compile the preview-only Android target
```

Important:

- The `:composeApp:` prefix on the dev-server task is required — an unqualified `wasmJsBrowserDevelopmentRun` can start a different module's dev server on the same port.
- `detekt` runs locally with autoCorrect (disabled on CI); if it auto-fixes formatting, import ordering, or trailing commas, the first run can report `BUILD FAILED` — simply rerun it. Do NOT manually fix import ordering.
- Key detekt rules: MaxLineLength 150, trailing commas required, MagicNumber (suppress at file level where UI code needs literals).
- There are currently no unit tests.
- Do not claim browser behavior was verified when only compilation or static analysis was run.

## Architecture Rules

- Layering: `feature` → `core:domain` → `core:data`. A feature module has no Gradle dependency on `core:data` at all (see `KmpFeaturePlugin`) — a ViewModel only ever calls a UseCase, never a Repository directly.
- Screen structure: public `Screen` (takes the `ViewModel`, collects `state`, handles Effects via `MviEffect`) → private `Screen` (measures width via `BoxWithConstraints`, branches on the `900.dp` breakpoint, forwards `state`/`onIntent`) → `XxxDesktopContent` / `XxxMobileContent` (layout per form factor; `onIntent` only when the UI dispatches intents — Splash is `state`-only; no `ViewModel`) → pure `component/*` (plain values + callbacks, never an `Intent`).
- MVI flow: UI dispatches an `Intent` → `ViewModel.onIntent` updates the internal `ViewModelState` → `ViewModelState.toState()` derives the public `State` → UI recomposes. One-shot side effects (navigation, opening a URL) live as an `effect` property inside `State` and are consumed exactly once through the `MviEffect` composable, which invokes the handler and then automatically dispatches `ConsumeEffect`. Every `XxxIntent` sealed interface must include `ConsumeEffect`.
- Write `onIntent` branch logic inline in the `when`; do not extract private per-branch handler functions. Private helpers are allowed for init/observe-style flows (e.g. `loadContributions` launched from `init {}`).

## Data, Domain, And Error Handling

- Repositories return plain `Flow<T>` with `.flowOn(@DefaultDispatcher)`. There is NO `Dispatchers.IO` on wasm — never introduce an `@IoDispatcher`; use the existing `DefaultDispatcher` qualifier from `core/common`.
- The custom `Result<T>` (`Loading`/`Success`/`Error`) + `.asResult()` is applied at the ViewModel subscription boundary, not inside the Repository/UseCase.
- UseCases are a public interface + `internal` Impl in the same file: the Metro trio `@ContributesBinding(AppScope::class)` / `@SingleIn(AppScope::class)` / `@Inject`, thin single-repository wrappers applying `.distinctUntilChanged()`.
- `ContributionsRepository` falls back to the static `FallbackContributions` snapshot whenever the fetch/parse fails (including always, on the preview-only Android target, since `fetchText()`'s actual returns `null` there). This is deliberate — do not convert it to error propagation.
- Static profile content (name, pinned repos, languages, SNS links) lives in `core/data`'s `ProfileContent.kt` as `internal val DefaultGitHubProfile = GitHubProfile(...)`.

## Navigation Rules

- Navigation 3, single `NavDisplay` + back stack owned by `composeApp`'s `AppNavDisplay`.
- Per feature: `navigation/XxxNavigationRoute.kt` holds the `@Serializable data object Xxx : NavKey` plus its colocated `NavBackStack<NavKey>.navigateXxx() = add(Xxx)` extension. Do not create a separate `XxxNavigationExtensions.kt`. `navigation/XxxNavigation.kt` holds the `EntryProviderScope<NavKey>.xxxEntries()` function, which obtains the ViewModel via `metroViewModel()` inside the `entry<...> { }` block.
- Cross-feature navigation is passed as a plain lambda parameter on `xxxEntries()` (e.g. `splashEntries(navigateProfile: () -> Unit)`) — a feature never depends on another feature module or a shared navigation module.
- CRITICAL: every new `NavKey` must be registered in `AppNavDisplay`'s `navKeySavedStateConfiguration` `SerializersModule` — wasmJs has no reflection, so forgetting this compiles fine but silently breaks (or crashes) back-stack save/restore for that destination.

## Compose UI Rules

- Use `KeiTheme.colors` / `.typography` / `.shapes` in `@Composable` code; use the default instance `keiColorScheme` (and friends) in non-composable code (syntax highlighter, `drawBehind`, etc.).
- Never hardcode a new color — add a field to `KeiColorScheme` instead.
- Selection colors: grey `KeiTheme.colors.selectionPill` for tree/list selection; the selected editor tab uses the blue pill (`KeiTheme.colors.tabSelected` fill + `KeiTheme.colors.tabSelectedBorder` border); Android green `KeiTheme.colors.androidGreen` (`#3DDC84`) is reserved for content-side accents (buttons, brand tile) and must NEVER be used for chrome selection states.
- The editor code pane (left) and the Preview pane (right) must always show the same data — update both together when changing profile content or layout.
- Previews: co-locate a plain `@Preview` (`androidx.compose.ui.tooling.preview.Preview`, no parameters) at the bottom of each component file, wrapped manually in `KeiTheme { ... }`. Screens/Content needing a `State` build one from `preview/XxxPreviewFixtures.kt` sample data rather than a live `ViewModel`. Do not introduce shared `@PreviewWrapper` infrastructure. Rendering requires the preview-only Android target (the `android {}` target from the `kei_1111.kmp.wasm` convention plugin; the tooling dependency is wired by `kei_1111.cmp`) — do not remove it.

## Naming Rules

- Packages: `io.github.kei_1111.*` (e.g. `io.github.kei_1111.feature.profile.destination.profile`, `io.github.kei_1111.core.domain.usecase`).
- Every screen defines a 5-file MVI set: `XxxViewModelState` / `XxxState` / `XxxIntent` / `XxxEffect` / `XxxViewModel`, plus `XxxScreen` and `XxxDesktopContent` / `XxxMobileContent`.
- Intent names are intent-based, not operation-based: `UpdateLayout`, `ToggleTree`, `ReceiveFontLoaded`. Names like `OnSaveButtonClick` are prohibited.
- Callbacks: `on` + action + target, e.g. `onClickPage`, `onChangeViewMode`.
- UseCases: `[verb][target]UseCase`, e.g. `GetProfileUseCase`. Only the `Get` verb exists today.
- No `strings.xml` — there are no Android resources at runtime. UI text is static Kotlin content (`ProfileContent.kt`), and Japanese literals are used directly in content data and composables where appropriate.

## Git And PR Rules

- Commit messages: Conventional Commits, `<type>: <description>` or `<type>(scope): <description>`. Allowed types are `feat`, `fix`, `docs`, `refactor`, `perf`, `test`, `build`, `ci`, and `chore`. Write the description in concise imperative English and keep each commit focused (e.g. `fix(profile): use the official note logo`).
- Branch names: `<type>/#<issue-number>` where the type mirrors the Issue type: `feature/`, `fix/` (`[Bug]`), `refactor/`, `docs/`, `research/`, `perf/`, `test/`, `ci/`, `chore/`.
- Issue titles and bodies are written concisely in English. Titles use `[<Type>]: <title>` (e.g. `[Bug]: note link icon differs from the official logo`). Include only the context needed to understand and act on the Issue.
- PR titles, bodies, review comments, and other GitHub-authored text are written concisely in English. A PR title matches its corresponding Issue title, and its body follows `.github/PULL_REQUEST_TEMPLATE.md`. Avoid repeating information already available in the Issue or diff.
- Do not push directly to `main`.
- Do not force-push a shared branch unless the user explicitly requests it and the impact is understood.
- Do not commit, push, create an Issue, or open a PR unless the user asks for that action.
- CI (`.github/workflows/ci.yml`) runs `./gradlew detekt :composeApp:compileKotlinWasmJs compileAndroidMain` on every PR to `main`. CD (`.github/workflows/cd.yml`) runs on push to `main` and deploys `:composeApp:wasmJsBrowserDistribution`'s output to GitHub Pages via `actions/deploy-pages`.

## Safety And Maintenance

- Never expose secrets, credentials, tokens, signing material, or machine-specific configuration.
- The Android target is preview-only: androidMain actuals may be no-op (`openUrl`, `fetchText` returning `null`, etc.) — never add Android-specific runtime features or network calls there.
- Declare all dependencies in `gradle/libs.versions.toml` and reference them via the version catalog, including inside convention plugins (`libs.findLibrary(...)`). Do NOT use the deprecated `compose.dependencies.*` Gradle accessors — specify artifacts directly.
- Prefer the existing convention plugins (`kei_1111.detekt`, `kei_1111.kmp.wasm`, `kei_1111.cmp`, `kei_1111.kmp.feature`, `kei_1111.metro`) over ad hoc Gradle configuration.
- Do not add heavy dependencies without approval.
- Do not rewrite large areas, rename public APIs, or move code across modules unless the task requires it.
- Never discard or overwrite unrelated working-tree changes.
- When generated templates or docs disagree with current source code, the source wins.
- Keep this file updated when agent-level instructions change.

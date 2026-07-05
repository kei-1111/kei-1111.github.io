# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Top-Level Rules

- You MUST invoke independent tools concurrently, not sequentially, to maximize efficiency.
- You MUST think exclusively in English. However, you MUST respond in Japanese.
- Before creating a plan, you MUST use agents to: 1) Read all files that will be modified and note their current structure, 2) Verify all APIs/classes referenced in the plan actually exist. Then present the plan with citations to specific files you verified.

## WHY: Project Purpose

**kei-1111.github.io** is a portfolio web application for kei-1111 (basic info, works, skills, SNS links), also serving as a Compose Multiplatform (CMP) learning project. Deployed at https://kei-1111.github.io/ via GitHub Pages.

The UI mimics the **Android Studio IDE (New UI / Islands Dark theme)**: a project tree, a code editor showing the profile as Kotlin source with syntax highlighting, and a Compose Preview pane rendering the actual profile card. Editor code and Preview content must stay in sync.

## WHAT: Tech Stack

Multimodule Clean Architecture + MVI with Kotlin / Compose Multiplatform, Metro DI, Navigation 3.

- **wasmJs** — the only distribution target (browser, GitHub Pages)
- **Android** — exists ONLY so the IDE can render commonMain `@Preview` (layoutlib). Never shipped.

See `docs/ArchitectureOverview.md` and `docs/ModuleOverview.md` for details.

### Module Roles

- `composeApp/` — Entry point. `AppGraph` (Metro `@DependencyGraph` DI root) and `AppNavDisplay` (Navigation 3 `NavDisplay` + `NavKey` back stack, wires `splashEntries()` / `profileEntries()`). wasmJs only (no Android target)
- `core/mvi/` — MVI base: `MviViewModel<VS, S, I>`, the `Intent` / `State` / `ViewModelState<S>` marker interfaces, and the `MviEffect` composable (consumes a one-shot Effect and auto-fires `onConsume`)
- `core/domain/` — UseCases (`GetProfileUseCase`, `GetContributionsUseCase`): thin `internal class` wrappers around a single Repository call, each bound via `@ContributesBinding(AppScope::class)`
- `core/data/` — Repositories: `ProfileRepository` (static `GitHubProfile` content, `ProfileContent.kt`), `ContributionsRepository` (fetches GitHub contribution calendar data, falls back to a static `FallbackContributions` snapshot when the fetch fails or on the preview-only Android target)
- `core/model/` — Data classes: `GitHubProfile` / `PinnedRepo` / `LanguageShare` / `LinkService`, `ContributionCalendar` / `ContributionDay`
- `core/common/` — `Result<T>` + `Flow<T>.asResult()`, the `DefaultDispatcher` qualifier and its `DispatcherBindings` Metro `@BindingContainer`
- `core/designsystem/` — AppTheme, `IdeColors` (Islands Dark palette), fonts (JetBrains Mono + Noto Sans JP + Zen Kaku Gothic New)
- `core/utils/` — `openUrl` expect/actual (wasmJs: `window.open`, android: no-op), plus `rememberIsPageVisible` / `prefersReducedMotion` expect/actual
- `feature/profile/` — Main IDE-style portfolio screen (tree / editor / preview pane / status bar)
- `feature/splash/` — Splash screen
- `build-logic/` — Convention plugins: `kei_1111.detekt`, `kei_1111.kmp.wasm`, `kei_1111.kmp.feature`, `kei_1111.metro`

Layering rule: `feature` → `core:domain` → `core:data`. A feature module has no Gradle dependency on `core:data` at all (see `KmpFeaturePlugin`) — a ViewModel only ever calls a UseCase, never a Repository directly.

MVI flow: the UI dispatches an `Intent` → `ViewModel.onIntent` updates the internal `ViewModelState` → `ViewModelState.toState()` derives the public `State` → the UI recomposes. One-shot side effects (navigation, opening a URL) live as an `effect` property inside `State` and are consumed exactly once through the `MviEffect` composable, which invokes the handler and then automatically sends the `ConsumeEffect` intent.

## HOW: Development Guide

### Build & Run Commands

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # Dev server (http://localhost:8080) — the :composeApp: prefix is required, an unqualified run can start a different module's dev server on the same port
./gradlew wasmJsBrowserDistribution                # Production build (used by CD)
./gradlew detekt                                   # Lint (autoCorrect enabled)
./gradlew :feature:profile:compileKotlinWasmJs     # Compile a single module (wasm)
./gradlew :feature:profile:compileAndroidMain      # Compile the preview-only Android target
```

There are currently no unit tests.

### CI/CD

- PR → `./gradlew detekt` (`.github/workflows/ci.yml`)
- Merge to main → `wasmJsBrowserDistribution` → GitHub Pages (`.github/workflows/cd.yml`)

### Static Analysis

- detekt runs with `autoCorrect = true` (configured in the convention plugin); run `./gradlew detekt` to auto-fix formatting, import ordering, and trailing commas
- You MUST NOT manually fix import ordering
- Key rules: MaxLineLength 120, trailing commas required, MagicNumber (suppress at file level where UI code needs literals)

### Compose Preview

- Use the unified annotation `androidx.compose.ui.tooling.preview.Preview` (CMP 1.10+) in commonMain
- Co-locate a plain `@Preview` (no parameters) at the bottom of each component file, wrapped in `AppTheme(darkTheme = true)`
- Rendering requires the Android target; it is provided by the `kei_1111.kmp.wasm` convention plugin (`androidLibrary`, namespace auto-derived from module path)

### Dependencies

- Declare all dependencies in `gradle/libs.versions.toml` and reference them via the catalog (including in convention plugins with `libs.findLibrary(...)`)
- Do NOT use `compose.dependencies.*` Gradle accessors — they are deprecated; specify artifacts directly

## Key Constraints

- The Android target is preview-only: androidMain actuals may be no-op (e.g. `openUrl`), and no Android-specific runtime features should be added
- Design rule: tree/list selection uses grey (`IdeColors.SelectionPill`); the selected editor tab uses the blue pill (`IdeColors.TabSelected` fill + `IdeColors.TabSelectedBorder` border), matching real AS Islands Dark; Android green `#3DDC84` is reserved for content-side accents (buttons, brand tile) — never use it for chrome selection states
- Commit messages are written in Japanese with a type prefix (e.g. `feat:`, `fix:`, `docs:`, `ci:`, `chore(deps):`)

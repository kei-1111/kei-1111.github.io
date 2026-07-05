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

Kotlin Multiplatform / Compose Multiplatform. Multimodule with Gradle convention plugins (`build-logic/`).

- **wasmJs** — the only distribution target (browser, GitHub Pages)
- **Android** — exists ONLY so the IDE can render commonMain `@Preview` (layoutlib). Never shipped.

See `docs/ArchitectureOverview.md` and `docs/ModuleOverview.md` for details.

### Module Roles

- `composeApp/` — Entry point, NavHost. wasmJs only (no Android target)
- `core/common/` — Shared interfaces, device-size breakpoints
- `core/data/` — All displayed content (images/strings) as static definitions; no API/DB
- `core/designsystem/` — AppTheme, `IdeColors` (Islands Dark palette), fonts (JetBrains Mono + Noto Sans JP)
- `core/model/` — Data classes (SNS, Skill, Work)
- `core/utils/` — `openUrl` expect/actual (wasmJs: `window.open`, android: no-op)
- `feature/profile/` — Main IDE-style portfolio screen (tree / editor / preview pane / status bar)
- `feature/splash/` — Splash screen
- `build-logic/` — Convention plugins: `kei_1111.detekt`, `kei_1111.kmp.wasm`, `kei_1111.kmp.feature`

By design there is no state-management layer: no ViewModel, no UiState. `:feature` reads static content from `:core:data` and renders it.

## HOW: Development Guide

### Build & Run Commands

```bash
./gradlew wasmJsBrowserDevelopmentRun            # Dev server (http://localhost:8080)
./gradlew wasmJsBrowserDistribution              # Production build (used by CD)
./gradlew detekt                                 # Lint (autoCorrect enabled)
./gradlew :feature:profile:compileKotlinWasmJs   # Compile a single module (wasm)
./gradlew :feature:profile:compileAndroidMain    # Compile the preview-only Android target
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

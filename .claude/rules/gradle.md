---
paths:
  - "**/*.gradle.kts"
  - "build-logic/**/*.kt"
  - "gradle/libs.versions.toml"
---

# Gradle & Build Configuration

## Version Catalog (mandatory)

Declare ALL dependencies and plugins in `gradle/libs.versions.toml` and reference them via the catalog:

- Module build files: `implementation(libs.xxx)` / `alias(libs.plugins.kei1111.xxx)`
- Convention plugins (build-logic): `libs.findLibrary("...").get()` / `libs.findPlugin("...")`
- Do **NOT** use the deprecated `compose.dependencies.*` Gradle accessors — specify artifacts directly through the catalog
- Convention plugin ids are declared as `[plugins]` entries with `version = "unspecified"` (e.g. `kei1111-detekt = { id = "kei_1111.detekt", version = "unspecified" }`)

## Convention Plugins

All module configuration goes through the five convention plugins in `build-logic/convention/src/main/kotlin/` — prefer extending them over ad hoc per-module Gradle configuration:

| Plugin id | Source | Responsibility |
|---|---|---|
| `kei_1111.detekt` | `DetektPlugin.kt` | detekt + formatting/compose rule sets, autoCorrect locally (disabled on CI), config from `config/detekt/detekt.yml`, jvmTarget 17 |
| `kei_1111.kmp.wasm` | `KmpWasmPlugin.kt` | KMP targets: `wasmJs { browser() }` + the **preview-only** `android {}` target (namespace auto-derived from project path — do not remove it, Compose Preview rendering needs it) |
| `kei_1111.cmp` | `CmpPlugin.kt` | Applies the Compose Multiplatform + Compose compiler plugins; on modules with the preview Android target, wires `compose.ui.tooling` for `@Preview` rendering |
| `kei_1111.kmp.feature` | `KmpFeaturePlugin.kt` | Applies `kei_1111.kmp.wasm` + `kei_1111.cmp` + serialization + `kei_1111.metro`; wires commonMain deps on `core:common/designsystem/domain/model/mvi/utils` (deliberately **NOT** `core:data` — layering rule) plus Compose/lifecycle/navigation3/metrox-viewmodel libraries |
| `kei_1111.metro` | `MetroPlugin.kt` | Metro DI compiler; `generateContributionProviders = true` keeps `internal` `@ContributesBinding` impls visible cross-module |

## Module Wiring

- A feature module's `build.gradle.kts` is minimal — just two plugin aliases (`kei1111.detekt` + `kei1111.kmp.feature`), no dependencies block. See `feature/profile/build.gradle.kts`
- New module: add `include(":feature:<name>")` to `settings.gradle.kts`, then reference it with **typesafe project accessors** (`implementation(projects.feature.<name>)` — enabled via `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`)
- Never add a `core:data` dependency to a feature module (see `.claude/rules/data-layer.md`)

## detekt

- Config: `config/detekt/detekt.yml` (`build.maxIssues: 0`); run with `./gradlew detekt`
- `autoCorrect` is enabled locally (disabled on CI) — a first run that reformats can end BUILD FAILED; rerun before judging. Never fix import ordering manually
- Key rules: MaxLineLength 120, trailing commas required, MagicNumber (suppress at file level where UI code needs literals)

## Build Commands

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # dev server (the :composeApp: prefix is required)
./gradlew :composeApp:wasmJsBrowserDistribution    # production build (CD)
./gradlew :feature:profile:compileKotlinWasmJs     # single-module wasm compile
./gradlew :feature:profile:compileAndroidMain      # preview-only Android target compile
```

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

## Dependency Updates

- Bump versions only in `gradle/libs.versions.toml`
- Kotlin is the anchor: Compose Multiplatform, AGP, and Metro each support specific Kotlin versions — check their compatibility notes before bumping, and bump coupled versions together
- One upgrade per branch/PR (a single library or one coupled group); no unrelated bulk bumps
- Validate: `./gradlew detekt :app:webApp:wasmJsBrowserDistribution compileAndroidMain :server:test`, plus a browser smoke test when the upgrade can affect runtime behavior (see `.claude/rules/ui-implementation.md` — Browser Smoke Test)

## Convention Plugins

All module configuration goes through the six convention plugins in `build-logic/convention/src/main/kotlin/` — prefer extending them over ad hoc per-module Gradle configuration:

| Plugin id | Source | Responsibility |
|---|---|---|
| `kei_1111.detekt` | `DetektPlugin.kt` | detekt + formatting/compose rule sets, autoCorrect locally (disabled on CI), config from `config/detekt/detekt.yml`, jvmTarget 17 |
| `kei_1111.kmp.wasm` | `KmpWasmPlugin.kt` | KMP targets: `wasmJs { browser() }` + the **preview-only** `android {}` target (namespace auto-derived from project path — do not remove it, Compose Preview rendering needs it) |
| `kei_1111.cmp` | `CmpPlugin.kt` | Applies the Compose Multiplatform + Compose compiler plugins; on modules with the preview Android target, wires `compose.ui.tooling` for `@Preview` rendering |
| `kei_1111.kmp.feature` | `KmpFeaturePlugin.kt` | Applies `kei_1111.kmp.wasm` + `kei_1111.cmp` + serialization + `kei_1111.metro`; wires commonMain deps on `app:core:common/designsystem/domain/mvi/utils` + `shared:model` + `test:tags` (deliberately **NOT** `app:core:data` — layering rule) plus Compose/lifecycle/navigation3/metrox-viewmodel libraries |
| `kei_1111.kmp.shared` | `KmpSharedPlugin.kt` | Applies `kei_1111.kmp.wasm` + a `jvm()` target — for `shared:model` (shared with `:server`) and `test:tags` (shared with `:test:e2e`) |
| `kei_1111.metro` | `MetroPlugin.kt` | Metro DI compiler; `generateContributionProviders = true` keeps `internal` `@ContributesBinding` impls visible cross-module |

## Module Wiring

- A feature module's `build.gradle.kts` is minimal — just two plugin aliases (`kei1111.detekt` + `kei1111.kmp.feature`), no dependencies block. See `app/feature/profile/build.gradle.kts`
- New module: add `include(":app:feature:<name>")` to `settings.gradle.kts`, then reference it with **typesafe project accessors** (`implementation(projects.app.feature.<name>)` — enabled via `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`)
- Never add an `app:core:data` dependency to a feature module (see `.claude/rules/data-layer.md`)

## detekt

- Config: `config/detekt/detekt.yml` (`build.maxIssues: 0`); run with `./gradlew detekt`
- `autoCorrect` is enabled locally (disabled on CI) — a first run that reformats can end BUILD FAILED; rerun before judging. Never fix import ordering manually
- Key rules: MaxLineLength 150, trailing commas required, MagicNumber (suppress at file level where UI code needs literals)

## Build Commands

```bash
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun  # dev server (the :app:webApp: prefix is required)
./gradlew :app:webApp:wasmJsBrowserDistribution    # production build (CD)
./gradlew :app:feature:profile:compileKotlinWasmJs     # single-module wasm compile
./gradlew :app:feature:profile:compileAndroidMain      # preview-only Android target compile
./gradlew :server:run                                  # Ktor server (http://localhost:8081; Cloud Run injects PORT)
./gradlew :server:buildFatJar                          # server/build/libs/server-all.jar (CD Server)
./gradlew :server:test                                 # server tests (CI runs this)
./gradlew :test:e2e:test -PbaseUrl=http://localhost:8083  # Playwright E2E against a served build (skipped without -PbaseUrl; not run in CI)
```

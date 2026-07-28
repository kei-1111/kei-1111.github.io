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

## `api()` is Prohibited

Every dependency is declared with `implementation()`, in module build files and convention plugins
alike, so a build file states exactly what its module depends on. A module that needs a type
declares it, even when an upstream module already has it. Enforced by
`scripts/check_gradle_conventions.sh`.

## Dependency Updates

- Bump versions only in `gradle/libs.versions.toml`
- Kotlin is the anchor: Compose Multiplatform, AGP, and Metro each support specific Kotlin versions — check their compatibility notes before bumping, and bump coupled versions together
- One upgrade per branch/PR (a single library or one coupled group); no unrelated bulk bumps
- Validate: `./gradlew detekt :app:webApp:wasmJsBrowserDistribution compileAndroidMain :server:test :app:core:domain:testAndroidHostTest :app:core:mvi:testAndroidHostTest :app:feature:profile:testAndroidHostTest`, plus a browser smoke test when the upgrade can affect runtime behavior (see `.claude/rules/ui-implementation.md` — Browser Smoke Test)

## Convention Plugins

All module configuration goes through the six convention plugins in `build-logic/convention/src/main/kotlin/` — prefer extending them over ad hoc per-module Gradle configuration:

| Plugin id | Source | Responsibility |
|---|---|---|
| `kei_1111.detekt` | `DetektPlugin.kt` | detekt + formatting/compose rule sets, autoCorrect locally (disabled on CI), config from `config/detekt/detekt.yml`, jvmTarget 17 |
| `kei_1111.kmp.wasm` | `KmpWasmPlugin.kt` | KMP targets: `wasmJs { browser() }` + the **non-shipped** `android {}` target (namespace auto-derived from project path — do not remove it; Compose Preview rendering needs it, and modules with unit tests run them on it as host tests) |
| `kei_1111.cmp` | `CmpPlugin.kt` | Applies the Compose Multiplatform + Compose compiler plugins; on modules with the non-shipped Android target, wires `compose.ui.tooling` for `@Preview` rendering |
| `kei_1111.kmp.feature` | `KmpFeaturePlugin.kt` | Applies `kei_1111.kmp.wasm` + `kei_1111.cmp` + serialization + `kei_1111.metro`; enables the Android host test (`withHostTestBuilder` — local-JVM ViewModel unit tests, see `.claude/rules/mvi-testing.md`; deliberately per-module, not in `kei_1111.kmp.wasm`); wires commonMain deps on `app:core:common/designsystem/domain/mvi/navigation/ui/utils` + `shared:model` + `test:tags` (deliberately **NOT** `app:core:data` — layering rule) plus Compose/lifecycle/navigation3/metrox-viewmodel libraries, and commonTest deps on `kotlin-test` + `kotlinx-coroutines-test` |
| `kei_1111.kmp.shared` | `KmpSharedPlugin.kt` | Applies `kei_1111.kmp.wasm` + a `jvm()` target — for `shared:model` (shared with `:server`) and `test:tags` (shared with `:test:e2e`) |
| `kei_1111.metro` | `MetroPlugin.kt` | Metro DI compiler; `generateContributionProviders = true` keeps `internal` `@ContributesBinding` impls visible cross-module |

## Module Wiring

- A feature module's `build.gradle.kts` is minimal — just two plugin aliases (`kei1111.detekt` + `kei1111.kmp.feature`), no dependencies block. See `app/feature/profile/build.gradle.kts`
- New module: add `include(":app:feature:<name>")` to `settings.gradle.kts`, then reference it with **typesafe project accessors** (`implementation(projects.app.feature.<name>)` — enabled via `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`)
- Never add an `app:core:data` dependency to a feature module (see `.claude/rules/data-layer.md`)
- Metro does not aggregate `@ContributesBinding` contributions from transitive `implementation` dependencies, and `api()` is prohibited in this repo — so the contributing module must be a direct dependency of the graph-owning module. This is why `app:webApp` depends directly on `app:core:data` even though only `app:core:domain` calls its Repositories

## detekt

- Config: `config/detekt/detekt.yml` (`build.maxIssues: 0`); run with `./gradlew detekt`
- `autoCorrect` is enabled locally (disabled on CI) — a first run that reformats can end BUILD FAILED; rerun before judging. Never fix import ordering manually
- Key rules: MaxLineLength 150, trailing commas required, MagicNumber (suppress at file level where UI code needs literals)

## Build Commands

```bash
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun  # dev server (the :app:webApp: prefix is required)
./gradlew :app:webApp:wasmJsBrowserDistribution    # production build (CD)
./gradlew :app:feature:profile:compileKotlinWasmJs     # single-module wasm compile
./gradlew :app:feature:profile:compileAndroidMain      # non-shipped Android target compile (Preview rendering)
./gradlew :server:run                                  # Ktor server (http://localhost:8081; Cloud Run injects PORT)
./gradlew :server:buildFatJar                          # server/build/libs/server-all.jar (Deploy Server)
./gradlew :server:test                                 # server tests (CI runs this)
./gradlew :app:feature:profile:testAndroidHostTest     # client unit tests, local JVM (CI runs these; also :app:core:domain / :app:core:mvi)
./gradlew :test:e2e:test -PbaseUrl=http://localhost:8083  # Playwright E2E against a served build (skipped without -PbaseUrl; not run in CI)
```

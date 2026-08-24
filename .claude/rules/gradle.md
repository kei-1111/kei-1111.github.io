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
- Adding an npm dependency (wasmJs) requires `./gradlew kotlinWasmUpgradeYarnLock` and committing `kotlin-js-store/` in the same change
- Validate every applicable change-type row in `.claude/rules/working-agreement.md`, every client
  test module selected by `.github/workflows/app-test.yml`, and the browser smoke test when runtime
  behavior can be affected.

## Convention Plugins

All module configuration goes through the plugins in
`build-logic/convention/src/main/kotlin/`; that directory is the canonical list and each source
file owns its exact behavior. Inspect the applicable plugin before changing module wiring, and
prefer extending it over ad hoc per-module configuration. The non-shipped Android constraint is
canonical in `.claude/rules/working-agreement.project.md`.

## Module Wiring

- A feature module's `build.gradle.kts` is minimal — just two plugin aliases (`kei1111.detekt` + `kei1111.kmp.feature`), no dependencies block. See `app/feature/profile/build.gradle.kts`
- New module: add `include(":app:feature:<name>")` to `settings.gradle.kts`, then reference it with **typesafe project accessors** (`implementation(projects.app.feature.<name>)` — enabled via `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`)
- When the new module contains host tests, add its task to `.github/workflows/app-test.yml` in the
  same change; that workflow is the canonical CI module set.
- Never add an `app:core:data` dependency to a feature module (see `.claude/rules/data-layer.md`)
- Metro does not aggregate `@ContributesBinding` contributions from transitive `implementation` dependencies, and `api()` is prohibited in this repo — so the contributing module must be a direct dependency of the graph-owning module. This is why `app:webApp` depends directly on `app:core:data` / `app:core:api` / `app:core:local` even though only `app:core:domain` calls the Repositories and only `app:core:data` calls the Api/DataSource bindings

## detekt

- Config and thresholds: `config/detekt/detekt.yml`; run with `./gradlew detekt`
- `autoCorrect` is enabled locally (disabled on CI) — a first run that reformats can end BUILD FAILED; rerun before judging. Never fix import ordering manually
- Key rules are executable in `config/detekt/detekt.yml`; suppress `MagicNumber` at file level where
  UI code genuinely needs literals.

## Development And Packaging Commands

```bash
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun  # dev server
./gradlew :app:webApp:wasmJsBrowserDistribution    # production build (CD)
./gradlew :server:run                                  # Ktor server (http://localhost:8081; Cloud Run injects PORT)
./gradlew :server:buildFatJar                          # server/build/libs/server-all.jar (Deploy Server)
```

Validation commands are selected by `.claude/rules/project-validation.md`;
the E2E serving and execution procedure is canonical in `.claude/rules/ui-testing.md` — Running.

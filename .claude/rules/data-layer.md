---
paths:
  - "core/data/**/*.kt"
  - "core/common/**/dispatcher/**/*.kt"
  - "composeApp/**/di/**/*.kt"
---

# Data Layer & DI Patterns

## Repository Implementation

- Define the public `XxxRepository` interface and its `internal` `XxxRepositoryImpl` in the **same file**. Reference: `core/data/src/commonMain/kotlin/.../repository/ProfileRepository.kt`.
- Annotate the impl class-level, in this order: `@ContributesBinding(AppScope::class)`, `@SingleIn(AppScope::class)`, `@Inject`.
- `internal` impls stay resolvable across modules because the `kei_1111.metro` convention plugin (`MetroPlugin.kt`) sets `generateContributionProviders = true` and `generateContributionHintsInFir = true`, so Metro generates a public top-level provider for the bound interface type.
- Return plain `Flow<T>` with an explicit `.flowOn(defaultDispatcher)` — no `runCatching`/`Result` wrapping (see `.claude/rules/error-handling.md`).

## Static vs. Fetched Content

- Most content is static: `ProfileRepository` wraps `internal val DefaultGitHubProfile` with `flowOf(...)`. Editing the portfolio's profile content means editing `ProfileContent.kt`, not the repository.
- `ContributionsRepository` is the only fetching repository, and the one sanctioned exception to "no swallowing errors": on any fetch/parse failure — and always on the preview-only Android target — it silently falls back to the static `FallbackContributions` snapshot. This design is **deliberate and approved**; do not "fix" it into rethrowing or emitting `Result.Error`.
- HTTP goes through the small `expect`/`actual` `fetchText` in `core/data/.../contributions/` (wasmJs: `XMLHttpRequest` with an 8000ms timeout, `null` on non-200; android: always `null` — the preview-only target must never perform network I/O). This project does **not** use Ktor.

## DI (Metro)

- `DispatcherBindings` (`core/common/.../dispatcher/`) provides dispatchers via a `@BindingContainer @ContributesTo(AppScope::class)` interface; `AppGraph` (`composeApp/.../di/AppGraph.kt`) is the `@DependencyGraph` root; `InjectedViewModelFactory` implements `MetroViewModelFactory` and is provided to the composition in `App.kt` via `LocalMetroViewModelFactory`.
- Repository/UseCase impls need no separate binding module — class-level `@ContributesBinding` is enough.
- `@DefaultDispatcher` (`core/common/.../dispatcher/DefaultDispatcher.kt`) is the **only** dispatcher qualifier, provided as `Dispatchers.Default`. There is **no** `@IoDispatcher` — wasmJs has no `Dispatchers.IO`; never introduce one.

## Layering Rule

`feature` modules have **no** Gradle dependency on `core:data` at all — enforced by the dependency list in `KmpFeaturePlugin.kt`. A ViewModel only ever calls a UseCase (see `.claude/rules/usecase.md`), never a Repository directly.

See also: `.claude/rules/error-handling.md` for how repository `Flow`s are wrapped further up the chain, `.claude/rules/usecase.md` for the layer directly above Repository.

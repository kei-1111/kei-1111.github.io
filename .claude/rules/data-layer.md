---
paths:
  - "app/core/data/**/*.kt"
  - "app/core/common/**/dispatcher/**/*.kt"
  - "app/webApp/**/di/**/*.kt"
---

# Data Layer & DI Patterns

## Repository Implementation

- Define the public `XxxRepository` interface and its `internal` `XxxRepositoryImpl` in the **same file**. Reference: `app/core/data/src/commonMain/kotlin/.../repository/ProfileRepository.kt`.
- Annotate the impl class-level, in this order: `@ContributesBinding(AppScope::class)`, `@SingleIn(AppScope::class)`, `@Inject`.
- `internal` impls stay resolvable across modules because the `kei_1111.metro` convention plugin (`MetroPlugin.kt`) sets `generateContributionProviders = true` and `generateContributionHintsInFir = true`, so Metro generates a public top-level provider for the bound interface type.
- Return plain `Flow<T>` with an explicit `.flowOn(defaultDispatcher)` — no `runCatching`/`Result` wrapping (see `.claude/rules/error-handling.md`).

## Fetch & Fallback Content

- `ProfileRepository` and `ContributionsRepository` both fetch from the project's own backend — the `:server` Ktor service on Cloud Run (`GET /api/profile`, `GET /api/contributions`) — which in turn calls the GitHub GraphQL API server-side behind a TTL cache. The wasm client never talks to GitHub directly.
- On any fetch/parse failure — and always on the preview-only Android target — each repository silently falls back to its static snapshot (`FallbackProfile.profile` / `FallbackContributions.calendar`). This design is **deliberate and approved**; do not "fix" it into rethrowing or emitting `Result.Error`. Editing the portfolio's profile content means editing the server's `ProfileContent.kt` (`DefaultGitHubProfile`) and mirroring the change into the client's `FallbackProfile.kt`.
- HTTP goes through the small `expect`/`actual` `fetchText` in `app/core/data/.../network/` (wasmJs: `XMLHttpRequest` with an 8000ms timeout and cancellation support, `null` on non-200/error/timeout; android: always `null` — the preview-only target must never perform network I/O). The wasm client does **not** use Ktor — Ktor is used only by `:server`.
- Each repository routes fetch+parse through a session-lifetime `SingleFlightCache` (`app/core/data/.../cache/SingleFlightCache.kt`) on a cache-owned scope: concurrent collectors share one request, only live results are cached (a failed fetch retries on the next collection), and a splash-time prefetch survives navigation. Deliberately no invalidation/TTL API.

## DI (Metro)

- `DispatcherBindings` (`app/core/common/.../dispatcher/`) provides dispatchers via a `@BindingContainer @ContributesTo(AppScope::class)` interface; `AppGraph` (`app/webApp/.../di/AppGraph.kt`) is the `@DependencyGraph` root; `InjectedViewModelFactory` implements `MetroViewModelFactory` and is provided to the composition in `App.kt` via `LocalMetroViewModelFactory`.
- Repository/UseCase impls need no separate binding module — class-level `@ContributesBinding` is enough.
- `@DefaultDispatcher` (`app/core/common/.../dispatcher/DefaultDispatcher.kt`) is the **only** dispatcher qualifier, provided as `Dispatchers.Default`. There is **no** `@IoDispatcher` — wasmJs has no `Dispatchers.IO`; never introduce one.

## Layering Rule

`feature` modules have **no** Gradle dependency on `app:core:data` at all — enforced by the dependency list in `KmpFeaturePlugin.kt`. A ViewModel only ever calls a UseCase (see `.claude/rules/usecase.md`), never a Repository directly.

See also: `.claude/rules/error-handling.md` for how repository `Flow`s are wrapped further up the chain, `.claude/rules/usecase.md` for the layer directly above Repository.

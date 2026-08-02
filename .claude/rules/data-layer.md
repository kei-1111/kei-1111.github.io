---
paths:
  - "app/core/data/**/*.kt"
  - "app/core/api/**/*.kt"
  - "app/core/local/**/*.kt"
  - "app/core/common/**/dispatcher/**/*.kt"
  - "app/webApp/**/di/**/*.kt"
---

# Data Layer & DI Patterns

## Repository Implementation

- Define the public `XxxRepository` interface and its `internal` `XxxRepositoryImpl` in the **same file**. Reference: `app/core/data/src/commonMain/kotlin/.../repository/ProfileRepository.kt`.
- Annotate the impl class-level, in this order: `@ContributesBinding(AppScope::class)`, `@SingleIn(AppScope::class)`, `@Inject`. Exception: `ThemeLocalDataSourceImpl` (`app:core:local`) puts `@Inject` on a no-arg secondary constructor — its primary constructor is the test seam for the `DataStore`/clear pair, and a default-argument seam is not an option because Metro treats defaulted parameters as graph dependencies (runtime `IrLinkageError`).
- `internal` impls stay resolvable across modules because the `kei_1111.metro` convention plugin (`MetroPlugin.kt`) sets `generateContributionProviders = true` and `generateContributionHintsInFir = true`, so Metro generates a public top-level provider for the bound interface type.
- Expose streams as `val` properties (`val profile: Flow<GitHubProfile>`), never `getXxx()` functions. Return plain `Flow<T>` with an explicit `.flowOn(defaultDispatcher)` — no `runCatching`/`Result` wrapping (see `.claude/rules/error-handling.md`). Static-content repositories (`LicensesRepository`) just return `flowOf(...)` — no fetch, no dispatcher.
- Writes (currently only `ThemeRepository.saveIsDark`) are plain `suspend fun`s delegating to `ThemeLocalDataSource` (`app:core:local`), which persists via DataStore `edit {}` — still no `Result` wrapping. The `DataStore<Preferences>` instance is created per target with expect/actual (`app/core/local/.../theme/ThemeDataStore.kt`): wasmJs uses `WebLocalStorage` (browser `localStorage`), the non-shipped Android target a compile-only throwing stub (`error(...)`) that is never executed. The data-source interface returns what is stored (`Flow<Boolean?>`, null when unsaved or unreadable); defaults are the Repository's job.
- Theme persistence is exception-safe inside `ThemeLocalDataSourceImpl`: a read failure emits `null` and drops the stored pair via the expect/actual `clearThemeDataStore()`; a write failure drops the pair and retries once — through the shared suppression helpers, keeping coroutine cancellation intact (see `.claude/rules/error-handling.md`).

## Fetch & Failure Propagation

- `ProfileRepository`, `ContributionsRepository`, `IssuesRepository`, and `WorksRepository` all fetch from the project's own backend — the `:server` Ktor service on Cloud Run (`GET /api/profile`, `GET /api/contributions`, `GET /api/issues`, `GET /api/works`) — which for the GitHub-backed endpoints calls the GitHub GraphQL API server-side behind a TTL cache (works is a server-static list). The wasm client never talks to GitHub directly.
- On any fetch/parse failure — and always on the non-shipped Android target — each repository throws (`checkNotNull(cache.get())`) inside its `Flow`; the ViewModel-side `.asResult()` turns that into `Result.Error` and the Profile UI renders per-part loading/error states with a retry (see `.claude/rules/error-handling.md`). There is no client-side content fallback. Editing the portfolio's profile content means editing the server's `ProfileContent.kt` (`DefaultGitHubProfile`).
- HTTP lives in `app:core:api`: per-endpoint clients (`ProfileApi` / `ContributionsApi` — public interface + `internal` impl in one file, same Metro annotations as Repositories) inject the single Ktor `HttpClient` provided by `network/HttpClientBindings.kt` (`ContentNegotiation` + kotlinx JSON with `ignoreUnknownKeys` for contract compatibility, `HttpTimeout` 8000ms) and deserialize via `response.body<T>()` — no hand-written parse functions. Only the engine is `expect`/`actual` (`network/CreateHttpClient.kt`): wasmJs uses `Js`, android a `MockEngine` answering 503 for every request — the non-shipped target must never perform network I/O. Each Api folds every failure (non-200/error/timeout/parse) to `null` via the shared `HttpClient.getOrNull<T>(url)` helper (`network/GetOrNull.kt`, built on `recoverOrElse` — see `.claude/rules/error-handling.md`); cancellation propagates. Never hand-write the try/catch fold in an Api client. Repositories inject the `XxxApi` interface and never touch the `HttpClient`.
- Each repository routes fetch+parse through a session-lifetime `SingleFlightCache` (`app/core/data/.../cache/SingleFlightCache.kt`) on a cache-owned scope: concurrent collectors share one request, only live results are cached (a failed fetch retries on the next collection), and a splash-time prefetch survives navigation. Deliberately no invalidation/TTL API.

## DI (Metro)

- `DispatcherBindings` (`app/core/common/.../dispatcher/`) provides dispatchers via a `@BindingContainer @ContributesTo(AppScope::class)` interface; `AppGraph` (`app/webApp/.../di/AppGraph.kt`) is the `@DependencyGraph` root; `InjectedViewModelFactory` implements `MetroViewModelFactory` and is provided to the composition in `App.kt` via `LocalMetroViewModelFactory`.
- Repository/UseCase impls need no separate binding module — class-level `@ContributesBinding` is enough.
- `@DefaultDispatcher` (`app/core/common/.../dispatcher/DefaultDispatcher.kt`) is the **only** dispatcher qualifier, provided as `Dispatchers.Default`. There is **no** `@IoDispatcher` — wasmJs has no `Dispatchers.IO`; never introduce one.

## Layering Rule

`feature` modules have **no** Gradle dependency on `app:core:data` at all — enforced by the dependency list in `KmpFeaturePlugin.kt`. A ViewModel only ever calls a UseCase (see `.claude/rules/usecase.md`), never a Repository directly (app-scoped cross-cutting utilities from `app:core:common` such as `InteractionLog` are the sanctioned non-data exception — see `.claude/rules/mvi-architecture.md`). Below the Repository, `app:core:data` depends on `app:core:api` (HTTP) and `app:core:local` (persistence); only `app:core:data` may depend on them. `app:webApp` also declares all three directly — Metro does not aggregate contributions from transitive `implementation` deps (see `.claude/rules/gradle.md`).

See also: `.claude/rules/error-handling.md` for how repository `Flow`s are wrapped further up the chain, `.claude/rules/usecase.md` for the layer directly above Repository.

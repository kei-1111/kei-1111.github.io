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
- Annotate the impl class-level, in this order: `@ContributesBinding(AppScope::class)`, `@SingleIn(AppScope::class)`, `@Inject`. Exception: the `app:core:local` data sources (`ThemeLocalDataSourceImpl`, `NotificationLocalDataSourceImpl`) put `@Inject` on a no-arg secondary constructor — their primary constructor is the test seam for the `DataStore`/clear pair, and a default-argument seam is not an option because Metro treats defaulted parameters as graph dependencies (runtime `IrLinkageError`).
- `internal` impls stay resolvable across modules because the `kei_1111.metro` convention plugin (`MetroPlugin.kt`) sets `generateContributionProviders = true` and `generateContributionHintsInFir = true`, so Metro generates a public top-level provider for the bound interface type.
- Expose streams as `val` properties (`val profile: Flow<GitHubProfile>`), never `getXxx()` functions. Return plain `Flow<T>` with an explicit `.flowOn(defaultDispatcher)` — no `runCatching`/`Result` wrapping (see `.claude/rules/error-handling.md`). Static-content repositories (`LicensesRepository`) just return `flowOf(...)` — no fetch, no dispatcher.
- Writes (`ThemeRepository.saveIsDark`, `NotificationRepository.saveLastNotifiedPrNumber`) are plain `suspend fun`s delegating to their `app:core:local` data source, which persists via DataStore `edit {}` — still no `Result` wrapping. Each store gets its own `DataStore<Preferences>` created per target with expect/actual (`app/core/local/.../theme/ThemeDataStore.kt`, `.../notification/NotificationDataStore.kt`): wasmJs uses `WebLocalStorage` (browser `localStorage`), the non-shipped Android target a compile-only throwing stub (`error(...)`) that is never executed. A data-source interface returns what is stored (null when unsaved or unreadable); defaults are the Repository's job — and `NotificationRepository` deliberately keeps `null` meaningful (first visit) instead of defaulting it.
- Persistence is exception-safe inside each `app:core:local` impl: a read failure emits `null` and drops the stored pair via that store's expect/actual `clearXxxDataStore()`; a write failure drops the pair and retries once — through the shared suppression helpers, keeping coroutine cancellation intact (see `.claude/rules/error-handling.md`).

## Fetch & Failure Propagation

- Remote repositories fetch from the project's own `:server` service on Cloud Run, which calls the
  GitHub GraphQL API behind a TTL cache. The route list is canonical in the server routing source;
  the wasm client never talks to GitHub directly.
- On any fetch/parse failure — and always on the non-shipped Android target — each repository throws (`checkNotNull(cache.get())`) inside its `Flow`; the ViewModel-side `.asResult()` turns that into `Result.Error` and the Profile UI renders per-part loading/error states with a retry (see `.claude/rules/error-handling.md`). There is no client-side content fallback. Editing the portfolio's profile content means editing the server's `ProfileContent.kt` (`DefaultGitHubProfile`); works and README content likewise live in the server's `WorksContent.kt` (`DefaultWorks`) and `ReadmeContent.kt` (`DefaultReadme`), and the server-defined terminal commands in `TerminalCommandsContent.kt` (`DefaultTerminalTextCommands`).
- HTTP lives in `app:core:api`. Each endpoint client keeps its public interface and `internal`
  implementation in one file, uses the Repository binding annotations, injects the shared Ktor
  `HttpClient`, and deserializes with `response.body<T>()`. Client plugins and timeout values are
  canonical in `network/HttpClientBindings.kt`; do not hand-write parsers.
- Only the engine is `expect`/`actual` (`network/CreateHttpClient.kt`): wasmJs uses the browser
  engine, and Android uses a no-network `MockEngine`.
- Each Api folds non-success responses, transport errors, timeouts, and parse failures to `null`
  through `HttpClient.getOrNull<T>(url)` (`network/GetOrNull.kt`, built on `recoverOrElse`);
  cancellation propagates. Never duplicate this fold in an endpoint client. Repositories inject
  the `XxxApi` interface and never touch the `HttpClient`.
- Each repository routes fetch+parse through a session-lifetime `SingleFlightCache` (`app/core/data/.../cache/SingleFlightCache.kt`) on a cache-owned scope: concurrent collectors share one request, only live results are cached (a failed fetch retries on the next collection), and a splash-time prefetch survives navigation. Deliberately no invalidation/TTL API.

## DI (Metro)

- `DispatcherBindings` (`app/core/common/.../dispatcher/`) provides dispatchers via a `@BindingContainer @ContributesTo(AppScope::class)` interface; `AppGraph` (`app/webApp/.../di/AppGraph.kt`) is the `@DependencyGraph` root; `InjectedViewModelFactory` implements `MetroViewModelFactory` and is provided to the composition in `App.kt` via `LocalMetroViewModelFactory`.
- Repository/UseCase impls need no separate binding module — class-level `@ContributesBinding` is enough.
- `@DefaultDispatcher` (`app/core/common/.../dispatcher/DefaultDispatcher.kt`) is the **only** dispatcher qualifier, provided as `Dispatchers.Default`. There is **no** `@IoDispatcher` — wasmJs has no `Dispatchers.IO`; never introduce one.

## Layering Rule

`feature` modules have **no** Gradle dependency on `app:core:data` at all — enforced by the dependency list in `KmpFeaturePlugin.kt`. A ViewModel only ever calls a UseCase (see `.claude/rules/usecase.md`), never a Repository directly (app-scoped cross-cutting utilities from `app:core:common` such as `InteractionLog` are the sanctioned non-data exception — see `.claude/rules/mvi-architecture.md`). Below the Repository, `app:core:data` depends on `app:core:api` (HTTP) and `app:core:local` (persistence); only `app:core:data` may depend on them. `app:webApp` also declares all three directly — Metro does not aggregate contributions from transitive `implementation` deps (see `.claude/rules/gradle.md`).

See also: `.claude/rules/error-handling.md` for how repository `Flow`s are wrapped further up the chain, `.claude/rules/usecase.md` for the layer directly above Repository.

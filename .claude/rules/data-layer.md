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
- Annotate the impl class-level, in this order: `@ContributesBinding(AppScope::class)`, `@SingleIn(AppScope::class)`, `@Inject`. Exception: `NotificationLocalDataSourceImpl` (`app:core:local`) puts `@Inject` on a no-arg secondary constructor — its primary constructor is the test seam for the `DataStore`/clear pair, and a default-argument seam is not an option because Metro treats defaulted parameters as graph dependencies (runtime `IrLinkageError`). The theme and language data sources do not need it: the graph hands them their dependencies.
- `internal` impls stay resolvable across modules because the `kei_1111.metro` convention plugin (`MetroPlugin.kt`) sets `generateContributionProviders = true` and `generateContributionHintsInFir = true`, so Metro generates a public top-level provider for the bound interface type.
- Expose streams as `val` properties (`val profile: Flow<Profile>`), never `getXxx()` functions. Return plain `Flow<T>` with an explicit `.flowOn(defaultDispatcher)` — no `runCatching`/`Result` wrapping (see `.claude/rules/error-handling.md`). Static-content repositories (`LicensesRepository`) just return `flowOf(...)` — no fetch, no dispatcher.
- Writes (`ThemeRepository.saveIsDark`, `LanguageRepository.saveLanguageTag`, `NotificationRepository.saveLastNotifiedPrNumber`) are plain `suspend fun`s delegating to their `app:core:local` data source, which persists via DataStore `edit {}` — still no `Result` wrapping.
- Two stores exist. Theme and language share the settings store, whose `DataStore<Preferences>` and `PersistedSettingsCleaner` seam the graph provides once through `SettingsDataStoreBindings` over `app/core/local/.../settings/SettingsDataStore.kt`; the notification store stays its own instance built inside `NotificationLocalDataSourceImpl` over `.../notification/NotificationDataStore.kt`. Both use `WebLocalStorage` (browser `localStorage`) on wasmJs and a compile-only throwing stub (`error(...)`) on the non-shipped Android target. Never build a second instance over one store name — `WebLocalStorage` has no `activeFiles` check, so the duplicate keeps separate in-memory state instead of failing.
- A data-source interface returns what is stored (`Flow<Boolean?>` / `Flow<String?>` / `Flow<Int?>`, null when unsaved or unreadable); resolving that null belongs to the layer above. `ThemeRepository` folds it to a fixed dark default, while `LanguageRepository` and `NotificationRepository` keep it meaningful — an unsaved language means "fall back to browser-locale detection", which only `Main.kt` can do, and an unsaved PR number means a first visit.
- Persistence is exception-safe inside each data source: a read failure emits `null` and drops that store's keys; a write failure retries plainly first and only then drops and retries again, so a transient failure cannot discard another setting sharing the store. Both paths go through the shared suppression helpers, keeping coroutine cancellation intact (see `.claude/rules/error-handling.md`).

## Fetch & Failure Propagation

- Remote repositories fetch from the project's own `:server` service on Cloud Run, which calls the
  GitHub GraphQL API behind a TTL cache. The route list is canonical in the server routing source;
  the wasm client never talks to GitHub directly.
- On any fetch/parse failure — and always on the non-shipped Android target — each repository throws (`checkNotNull(cache.get())`) inside its `Flow`; the ViewModel-side `.asResult()` turns that into `Result.Error` and the Profile UI renders per-part loading/error states with a retry (see `.claude/rules/error-handling.md`). There is no client-side content fallback. Editing the portfolio's content means publishing it from the admin console — the server serves only what is published and answers 503 otherwise, so a content outage surfaces as the error state rather than as stale content.
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
- A binding's type must be on the compile classpath of `app:webApp`, which owns the graph, even when the provider and every consumer live in another module. Omitting the dependency still compiles and only fails at runtime with `IrLinkageError` on the generated factory — this is why `app:webApp` declares the DataStore artifacts it never references directly, and why a graph change is verified in a browser, not by a compile.

## Layering Rule

`feature` modules have **no** Gradle dependency on `app:core:data` at all — enforced by the dependency list in `KmpFeaturePlugin.kt`. A ViewModel only ever calls a UseCase (see `.claude/rules/usecase.md`), never a Repository directly (app-scoped cross-cutting utilities from `app:core:common` such as `InteractionLog` are the sanctioned non-data exception — see `.claude/rules/mvi-architecture.md`). Below the Repository, `app:core:data` depends on `app:core:api` (HTTP) and `app:core:local` (persistence); only `app:core:data` may depend on them. `app:webApp` also declares all three directly — Metro does not aggregate contributions from transitive `implementation` deps (see `.claude/rules/gradle.md`).

See also: `.claude/rules/error-handling.md` for how repository `Flow`s are wrapped further up the chain, `.claude/rules/usecase.md` for the layer directly above Repository.

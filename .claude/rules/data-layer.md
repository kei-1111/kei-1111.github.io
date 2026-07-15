---
paths: "app/core/data/**/*.kt,app/core/common/**/dispatcher/**/*.kt,app/webApp/**/di/**/*.kt"
---

# Data Layer & DI Patterns

## Repository Implementation

### Interface and Implementation Definition

Define the interface and its implementation class in the **same file**.

| Type | Name | Modifier |
|---|---|---|
| Interface | `XxxRepository` | public |
| Implementation | `XxxRepositoryImpl` | `internal` |

**Example**: `app/core/data/src/commonMain/kotlin/.../repository/ProfileRepository.kt`

```kt
interface ProfileRepository {
    val profile: Flow<GitHubProfile>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ProfileRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ProfileRepository {
    override val profile: Flow<GitHubProfile> = flow {
        val live = fetchText("$API_BASE_URL/api/profile")?.let(::parseProfile)
        emit(live ?: FallbackProfile.profile)
    }.flowOn(defaultDispatcher)
}
```

### Implementation Annotations (Metro)

Annotate `XxxRepositoryImpl` (class-level, in this order):

- `@ContributesBinding(AppScope::class)` — contributes the impl as the bound interface to `AppGraph`
- `@SingleIn(AppScope::class)` — single instance per app scope
- `@Inject` — constructor injection

`internal` visibility is preserved across modules (e.g. a `feature` module referencing a binding that only
resolves to an `app:core:data` impl) because the `kei_1111.metro` convention plugin sets
`generateContributionProviders = true` and `generateContributionHintsInFir = true`, so Metro generates a
public top-level provider for the bound interface type.

**Convention plugin**: `build-logic/convention/src/main/kotlin/MetroPlugin.kt`

### Flow Definition Rules

- Specify the dispatcher explicitly with `.flowOn(defaultDispatcher)`
- Return plain `Flow<T>` — do **not** wrap with `runCatching` or `Result` (see `.claude/rules/error-handling.md`)
- A repository falls back internally to a static snapshot on failure (see below) —
  this is the one sanctioned exception to "no swallowing errors"

## Fetch → Fallback Repositories

Both repositories fetch from the project's own API (the `:server` Ktor service on Cloud Run) and fall
back to a static snapshot: `ProfileRepository` → `FallbackProfile.profile`, `ContributionsRepository` →
`FallbackContributions.calendar`.

**Example**: `app/core/data/src/commonMain/kotlin/.../repository/ContributionsRepository.kt`

```kt
override fun getContributions(): Flow<ContributionCalendar> = flow {
    val live = fetchText("$API_BASE_URL/api/contributions")?.let(::parseContributions)
    emit(live ?: FallbackContributions.calendar)
}.flowOn(defaultDispatcher)
```

This fallback-to-static-snapshot design is **deliberate and approved** — it is not a bug and must not be
"fixed" into error propagation (e.g. do not change it to rethrow or to emit `Result.Error`). Loading,
timeout, offline, and the preview-only Android target all resolve the same way: fall back silently to
the static snapshot.

The profile source content itself lives in the server's `ProfileContent.kt`
(`server/src/main/kotlin/.../profile/ProfileContent.kt`, `internal val DefaultGitHubProfile`) — editing
the portfolio's profile content means editing that file **and** the client's `FallbackProfile` copy
(`app/core/data/src/commonMain/kotlin/.../profile/FallbackProfile.kt`) together.

- `app/core/data/src/commonMain/kotlin/.../network/FetchText.kt` — `internal expect suspend fun fetchText(url: String): String?`
- `app/core/data/src/wasmJsMain/kotlin/.../network/FetchText.wasmJs.kt` — actual: `XMLHttpRequest` with an
  8000ms timeout, returns `responseText` on HTTP 200 else `null`
- `app/core/data/src/androidMain/kotlin/.../network/FetchText.android.kt` — actual: always `null` (the
  Android target is preview-only and must never perform network I/O)
- `app/core/data/src/commonMain/kotlin/.../network/ApiConfig.kt` — the `API_BASE_URL` constant (Cloud Run URL)
- `app/core/data/src/commonMain/kotlin/.../profile/ProfileApi.kt` and `.../contributions/ContributionsApi.kt` —
  `parseProfile(body)` / `parseContributions(body)`, using `Json { ignoreUnknownKeys = true }` and
  catching `SerializationException` / `IllegalArgumentException` into `null`

The wasm client does **not** use Ktor for HTTP (Ktor is used only by `:server`); client fetching goes
through this small `expect`/`actual` `fetchText` function instead.

## DI Module Structure (Metro)

| Component | Role | Mechanism |
|---|---|---|
| `app/core/common/.../dispatcher/DispatcherBindings.kt` | Provides `CoroutineDispatcher` | `@BindingContainer @ContributesTo(AppScope::class) interface` with a `companion object` `@Provides` function |
| Repository / UseCase impls | Concrete implementations | class-level `@ContributesBinding(AppScope::class)` (no separate binding module needed) |
| `app/webApp/.../di/AppGraph.kt` | DI root | `@DependencyGraph(scope = AppScope::class, bindingContainers = [DispatcherBindings::class]) interface AppGraph : ViewModelGraph` |
| `app/webApp/.../di/InjectedViewModelFactory.kt` | ViewModel creation | `@ContributesBinding @SingleIn(AppScope::class) @Inject internal class` implementing `MetroViewModelFactory` |

`app/webApp/src/commonMain/kotlin/.../App.kt` provides the factory to the composition:

```kt
CompositionLocalProvider(
    LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
) { ... }
```

### Dispatcher Qualifier

`@DefaultDispatcher` (`app/core/common/.../dispatcher/DefaultDispatcher.kt`, `@dev.zacsweers.metro.Qualifier`)
is the **only** dispatcher qualifier in this project, provided as `Dispatchers.Default` by
`DispatcherBindings`. There is **no** `@IoDispatcher` — wasmJs has no `Dispatchers.IO`. Never introduce one;
use `@DefaultDispatcher` for all repository dispatching.

## Layering Rule

`feature` modules have **no** Gradle dependency on `app:core:data` at all — enforced by the dependency list in
`build-logic/convention/src/main/kotlin/KmpFeaturePlugin.kt` (which wires `app:core:common`, `app:core:designsystem`,
`app:core:domain`, `shared:model`, `app:core:mvi`, `app:core:utils`, but not `app:core:data`). A `ViewModel` only ever calls a
`UseCase` (see `.claude/rules/usecase.md`); it never references a Repository type directly.

See also: `.claude/rules/error-handling.md` for how repository `Flow`s are wrapped further up the chain,
`.claude/rules/usecase.md` for the layer directly above Repository.

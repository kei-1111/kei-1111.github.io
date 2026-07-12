---
paths: "app/core/data/**/*.kt,app/core/common/**/dispatcher/**/*.kt,app/composeApp/**/di/**/*.kt"
---

# Data Layer & DI Patterns

## Repository Implementation

### Interface and Implementation Definition

Define the interface and its implementation class in the **same file**.

| Type | Name | Modifier |
|---|---|---|
| Interface | `XxxRepository` | public |
| Implementation | `XxxRepositoryImpl` | `internal` |

**Example**: `core/data/src/commonMain/kotlin/.../repository/ProfileRepository.kt`

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
    override val profile: Flow<GitHubProfile> = flowOf(DefaultGitHubProfile).flowOn(defaultDispatcher)
}
```

### Implementation Annotations (Metro)

Annotate `XxxRepositoryImpl` (class-level, in this order):

- `@ContributesBinding(AppScope::class)` — contributes the impl as the bound interface to `AppGraph`
- `@SingleIn(AppScope::class)` — single instance per app scope
- `@Inject` — constructor injection

`internal` visibility is preserved across modules (e.g. a `feature` module referencing a binding that only
resolves to a `core:data` impl) because the `kei_1111.metro` convention plugin sets
`generateContributionProviders = true` and `generateContributionHintsInFir = true`, so Metro generates a
public top-level provider for the bound interface type.

**Convention plugin**: `build-logic/convention/src/main/kotlin/MetroPlugin.kt`

### Flow Definition Rules

- Specify the dispatcher explicitly with `.flowOn(defaultDispatcher)`
- Return plain `Flow<T>` — do **not** wrap with `runCatching` or `Result` (see `.claude/rules/error-handling.md`)
- A repository may fall back internally to static data on failure (see `ContributionsRepository` below) —
  this is the one sanctioned exception to "no swallowing errors"

## Static vs. Fetched Repositories

Most content is static. `ProfileRepository` wraps a hand-written `GitHubProfile` constant with `flowOf(...)`.

**Example**: `core/data/src/commonMain/kotlin/.../repository/ProfileContent.kt` defines
`internal val DefaultGitHubProfile = GitHubProfile(...)` — editing the portfolio's profile content means
editing this file, not the repository.

`ContributionsRepository` is the one repository that fetches live data, with a graceful static fallback:

**Example**: `core/data/src/commonMain/kotlin/.../repository/ContributionsRepository.kt`

```kt
override fun getContributions(user: String): Flow<ContributionCalendar> = flow {
    val live = fetchText("$CONTRIBUTIONS_API$user?y=last")?.let(::parseContributions)
    emit(live ?: FallbackContributions.calendar)
}.flowOn(defaultDispatcher)
```

This fallback-to-static-snapshot design is **deliberate and approved** — it is not a bug and must not be
"fixed" into error propagation (e.g. do not change it to rethrow or to emit `Result.Error`). Loading,
timeout, offline, and the preview-only Android target all resolve the same way: fall back silently to
`FallbackContributions.calendar`.

- `core/data/src/commonMain/kotlin/.../contributions/FetchText.kt` — `internal expect suspend fun fetchText(url: String): String?`
- `core/data/src/wasmJsMain/kotlin/.../contributions/FetchText.wasmJs.kt` — actual: `XMLHttpRequest` with an
  8000ms timeout, returns `responseText` on HTTP 200 else `null`
- `core/data/src/androidMain/kotlin/.../contributions/FetchText.android.kt` — actual: always `null` (the
  Android target is preview-only and must never perform network I/O)
- `core/data/src/commonMain/kotlin/.../contributions/ContributionsApi.kt` — `CONTRIBUTIONS_API` constant
  and `parseContributions(body): ContributionCalendar?`, using `Json { ignoreUnknownKeys = true }` and
  catching `SerializationException` / `IllegalArgumentException` into `null`

This project does **not** use Ktor for HTTP; fetching goes through this small `expect`/`actual` `fetchText`
function instead.

## DI Module Structure (Metro)

| Component | Role | Mechanism |
|---|---|---|
| `core/common/.../dispatcher/DispatcherBindings.kt` | Provides `CoroutineDispatcher` | `@BindingContainer @ContributesTo(AppScope::class) interface` with a `companion object` `@Provides` function |
| Repository / UseCase impls | Concrete implementations | class-level `@ContributesBinding(AppScope::class)` (no separate binding module needed) |
| `composeApp/.../di/AppGraph.kt` | DI root | `@DependencyGraph(scope = AppScope::class, bindingContainers = [DispatcherBindings::class]) interface AppGraph : ViewModelGraph` |
| `composeApp/.../di/InjectedViewModelFactory.kt` | ViewModel creation | `@ContributesBinding @SingleIn(AppScope::class) @Inject internal class` implementing `MetroViewModelFactory` |

`composeApp/src/commonMain/kotlin/.../App.kt` provides the factory to the composition:

```kt
CompositionLocalProvider(
    LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
) { ... }
```

### Dispatcher Qualifier

`@DefaultDispatcher` (`core/common/.../dispatcher/DefaultDispatcher.kt`, `@dev.zacsweers.metro.Qualifier`)
is the **only** dispatcher qualifier in this project, provided as `Dispatchers.Default` by
`DispatcherBindings`. There is **no** `@IoDispatcher` — wasmJs has no `Dispatchers.IO`. Never introduce one;
use `@DefaultDispatcher` for all repository dispatching.

## Layering Rule

`feature` modules have **no** Gradle dependency on `core:data` at all — enforced by the dependency list in
`build-logic/convention/src/main/kotlin/KmpFeaturePlugin.kt` (which wires `core:common`, `core:designsystem`,
`core:domain`, `core:model`, `core:mvi`, `core:utils`, but not `core:data`). A `ViewModel` only ever calls a
`UseCase` (see `.claude/rules/usecase.md`); it never references a Repository type directly.

See also: `.claude/rules/error-handling.md` for how repository `Flow`s are wrapped further up the chain,
`.claude/rules/usecase.md` for the layer directly above Repository.

---
paths:
  - "app/core/data/**/*.kt"
  - "app/core/common/**/result/**/*.kt"
  - "app/core/common/**/coroutines/**/*.kt"
  - "app/feature/**/*ViewModel.kt"
  - "app/feature/**/*ViewModelState.kt"
  - "app/webApp/**/*.kt"
---

# Error Handling Patterns

## Result + asResult() Layering

| Layer | Rule |
|---|---|
| Repository | Return plain `Flow<T>` — no `runCatching`, no `Result` wrapping |
| UseCase | Pass-through `Flow<T>` + `.distinctUntilChanged()` — still no `Result` wrapping |
| ViewModel | Apply `.asResult()` at the subscription point, store the whole `Result` in `ViewModelState`, handle with a `when (result)` expression |

Content is read-only on this portfolio site; the one write is the theme selection (`ThemeRepository.saveIsDark` — a plain `suspend fun` persisting via DataStore `edit {}`, no `Result` wrapping; the webApp caller treats it as best-effort). Do not introduce mutation-oriented `runCatching` + `onSuccess`/`onFailure` patterns without first defining a project-specific convention.

## Result Type

The custom sealed interface `Result<T>` (`Success(data)` / `Error(exception)` / `Loading`), its `successOrNull` accessor (use it instead of hand-written `as? Result.Success` casts), and `Flow<T>.asResult()` live in `app/core/common/src/commonMain/kotlin/.../result/` — **not** `kotlin.Result`. `asResult()` maps emissions to `Success`, prepends `Loading` via `onStart`, and catches into `Error`.

## Fetch Failure Propagation

`ProfileRepositoryImpl`, `ContributionsRepositoryImpl`, `IssuesRepositoryImpl`, and `WorksRepositoryImpl` all return a plain `Flow<T>` that throws on backend fetch/parse failure (see `.claude/rules/data-layer.md`); `.asResult()` at the ViewModel turns that into `Result.Error`. Because these flows can throw, **every** ViewModel collector guards with `.asResult()` — via the `MviViewModel` helpers `collectAsResult()` / `prefetchAsResult()`, or directly when a side effect must ride along (`loadLicenses` logs on `Result.Error`). A bare `collect`/`launchIn` lets the exception kill the coroutine scope.

## ViewModel Layer

- Apply `.asResult()` where the UseCase `Flow` is collected, and keep the whole `Result` in `ViewModelState` (e.g. `profileResult: Result<GitHubProfile> = Result.Loading`), not just the unwrapped data. Reference: `app/feature/profile/.../destination/profile/ProfileViewModel.kt`.
- `ProfileViewModel` launches the profile, contributions, issues, and works loads in parallel from `init` — UseCase calls are combined in the ViewModel, never by one UseCase calling another. `SplashViewModel` fire-and-forgets the profile and contributions UseCases through `prefetchAsResult()`; the repositories' `SingleFlightCache` keeps those fetches alive across navigation and never caches a failed result.
- `toState()` unwraps `Success` into the data fields (`Loading` surfaces as `null` = "no data yet") and derives failure flags from `Error` (`profileLoadFailed` / `contributionsLoadFailed` / `issuesLoadFailed` / `worksLoadFailed`). The Profile UI renders these as per-part states — editor code skeleton, Preview building indicator, and an error row whose retry dispatches `ProfileIntent.RetryBackendData`, which re-collects only the streams whose `Result` is `Error`.
- There is no `statusType` enum — do not introduce one.

## Cancellation-Safe Suppression Helpers

`recoverOrElse(block, onFailure)` and `runBestEffort(block)` (`app/core/common/src/commonMain/kotlin/.../coroutines/Suppression.kt`) encode the "swallow the failure but always propagate coroutine cancellation" policy once (`ensureActive()` before recovering). The documented suppression sites must use them — no hand-written broad `try/catch`. The one exception is the `isDark` `Flow.catch` in `ThemeLocalDataSourceImpl`, which stays a hand-written operator (already cancellation-transparent). The helpers' existence does not authorize new suppression sites.

`SingleFlightCache` is the other deliberate hand-written exception: its fetch runs in a
cache-owned scope, so caller cancellation must not stop it, while cancellation of that owned scope
must still propagate. Its local catches distinguish those two cases and fold fetch-originated
failures into a retryable `null`; the general suppression helpers do not express that ownership
boundary.

## Prohibited Patterns

| Pattern | Alternative |
|---|---|
| `runCatching` inside a Repository `Flow` | Return plain `Flow<T>`; let `.asResult()` handle it at the ViewModel boundary |
| `kotlin.Result` in Repository/UseCase signatures | The custom `app.core.common.result.Result` at the ViewModel boundary only |
| Swallowing an exception anywhere else | Not permitted — sanctioned sites are named in this rule and `.claude/rules/data-layer.md`; keep each site's documented cancellation semantics intact |

See also: `.claude/rules/data-layer.md` for the Repository fetch design, `.claude/rules/usecase.md` for why UseCases stay `Result`-free, `.claude/rules/mvi-architecture.md` for `ViewModelState`/`State` shape.

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

Content is read-only on this portfolio site; the writes are local preferences only — the theme selection (`ThemeRepository.saveIsDark`) and the last notified pull-request number (`NotificationRepository.saveLastNotifiedPrNumber`), both plain `suspend fun`s persisting via DataStore `edit {}` with no `Result` wrapping and best-effort callers. Do not introduce mutation-oriented `runCatching` + `onSuccess`/`onFailure` patterns without first defining a project-specific convention.

## Result Type

The custom sealed interface `Result<T>` (`Success(data)` / `Error(exception)` / `Loading`), its `successOrNull` accessor (use it instead of hand-written `as? Result.Success` casts), and `Flow<T>.asResult()` live in `app/core/common/src/commonMain/kotlin/.../result/` — **not** `kotlin.Result`. `asResult()` maps emissions to `Success`, prepends `Loading` via `onStart`, and catches into `Error`.

## Fetch Failure Propagation

`ProfileRepositoryImpl`, `ContributionsRepositoryImpl`, `IssuesRepositoryImpl`, `WorksRepositoryImpl`, `ReadmeRepositoryImpl`, `TerminalCommandsRepositoryImpl`, and `ChangelogRepositoryImpl` all return a plain `Flow<T>` that throws on backend fetch/parse failure (see `.claude/rules/data-layer.md`); `.asResult()` at the ViewModel turns that into `Result.Error`. Because these flows can throw, **every** ViewModel collector guards with `.asResult()` — via the `MviViewModel` helpers `collectAsResult()` / `prefetchAsResult()`, or directly when a side effect must ride along (`loadLicenses` logs on `Result.Error`). A bare `collect`/`launchIn` lets the exception kill the coroutine scope.

## ViewModel Layer

- Apply `.asResult()` where the UseCase `Flow` is collected, and keep the whole `Result` in `ViewModelState` (e.g. `profileResult: Result<GitHubProfile> = Result.Loading`), not just the unwrapped data. Reference: `app/feature/profile/.../destination/profile/ProfileViewModel.kt`.
- `ProfileViewModel` launches the profile, contributions, issues, works, readme, terminal-command, and changelog loads in parallel from `init` — UseCase calls are combined in the ViewModel, never by one UseCase calling another. `SplashViewModel` fire-and-forgets the profile, contributions, and readme UseCases through `prefetchAsResult()`, and collects works with `collectAsResult()` because it derives the image URLs to warm from that result; the repositories' `SingleFlightCache` keeps those fetches alive across navigation and never caches a failed result.
- `toState()` unwraps `Success` into the data fields (`Loading` surfaces as `null` = "no data yet") and derives failure flags from `Error` (`profileLoadFailed` / `contributionsLoadFailed` / `issuesLoadFailed` / `worksLoadFailed` / `readmeLoadFailed` / `changelogLoadFailed`). The Profile UI renders these as per-part states — editor code skeleton, Preview building indicator, and an error row whose retry dispatches `ProfileIntent.RetryBackendData`, which re-collects only the streams whose `Result` is `Error`. The one silent stream is `terminalCommandsResult`: it has no failure flag or error UI — on `Error` the terminal simply lacks the server-defined commands (builtins keep working) while `RetryBackendData` still re-collects it.
- There is no `statusType` enum — do not introduce one.

## Cancellation-Safe Suppression Helpers

`recoverOrElse(block, onFailure)` and `runBestEffort(block)` (`app/core/common/src/commonMain/kotlin/.../coroutines/Suppression.kt`) encode the "swallow the failure but always propagate coroutine cancellation" policy once (`ensureActive()` before recovering). The documented suppression sites must use them — no hand-written broad `try/catch`. The one exception is the read-side `Flow.catch` in the `app:core:local` data-source impls, which stays a hand-written operator (already cancellation-transparent). The helpers' existence does not authorize new suppression sites.

`App` (`app:webApp`) wraps `saveBootThemeColor` (`app:core:utils`) in `runBestEffort` for the same
reason it wraps `saveIsDark`: the browser `localStorage` write can throw (quota, storage disabled),
and a purely cosmetic boot hint must not cancel the composition scope that owns the theme. Its
wasmJs actual normalizes the `JsException` into an `Exception` first, for the reason spelled out
below for `RetryingResourceReader`.

`SingleFlightCache` is the other deliberate hand-written exception: its fetch runs in a
cache-owned scope, so caller cancellation must not stop it, while cancellation of that owned scope
must still propagate. Its local catches distinguish those two cases and fold fetch-originated
failures into a retryable `null`; the general suppression helpers do not express that ownership
boundary.

`retryWithBackoff(block)` (`Retry.kt`, same directory) builds on `recoverOrElse` to retry a failed
`block` with capped exponential backoff until success; cancellation is the only exit besides
success. Its sole sanctioned consumer is `RetryingResourceReader` (`app:webApp`), which wraps the
CMP `ResourceReader` because a failed resource fetch is otherwise never re-attempted and the app
stays incomplete forever (Issue #187); it normalizes wasm's `JsException` (a `Throwable`, not an
`Exception`) into an `Exception` first so rejected fetches are retryable. Neither helper
authorizes new suppression or retry sites.

## Prohibited Patterns

| Pattern | Alternative |
|---|---|
| `runCatching` inside a Repository `Flow` | Return plain `Flow<T>`; let `.asResult()` handle it at the ViewModel boundary |
| `kotlin.Result` in Repository/UseCase signatures | The custom `app.core.common.result.Result` at the ViewModel boundary only |
| Swallowing an exception anywhere else | Not permitted — sanctioned sites are named in this rule and `.claude/rules/data-layer.md`; keep each site's documented cancellation semantics intact |

See also: `.claude/rules/data-layer.md` for the Repository fetch design, `.claude/rules/usecase.md` for why UseCases stay `Result`-free, `.claude/rules/mvi-architecture.md` for `ViewModelState`/`State` shape.

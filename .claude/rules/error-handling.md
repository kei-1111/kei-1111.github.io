---
paths:
  - "app/core/data/**/*.kt"
  - "app/core/common/**/result/**/*.kt"
  - "app/feature/**/*ViewModel.kt"
  - "app/feature/**/*ViewModelState.kt"
---

# Error Handling Patterns

## Result + asResult() Layering

| Layer | Rule |
|---|---|
| Repository | Return plain `Flow<T>` — no `runCatching`, no `Result` wrapping |
| UseCase | Pass-through `Flow<T>` + `.distinctUntilChanged()` — still no `Result` wrapping |
| ViewModel | Apply `.asResult()` at the subscription point, store the whole `Result` in `ViewModelState`, handle with a `when (result)` expression |

Content is read-only on this portfolio site; the one write is the theme selection (`ThemeRepository.saveIsDark` — a plain `suspend fun` persisting via DataStore `edit {}`, no `Result` wrapping; the webApp caller treats it as best-effort, and the repository itself self-heals corrupted persisted state — see `.claude/rules/data-layer.md`). Do not introduce mutation-oriented `runCatching` + `onSuccess`/`onFailure` patterns without first defining a project-specific convention.

## Result Type

The custom sealed interface `Result<T>` (`Success(data)` / `Error(exception)` / `Loading`) and `Flow<T>.asResult()` live in `app/core/common/src/commonMain/kotlin/.../result/` — **not** `kotlin.Result`. `asResult()` maps emissions to `Success`, prepends `Loading` via `onStart`, and catches into `Error`.

## Fetch Failure Propagation

`ProfileRepositoryImpl`, `ContributionsRepositoryImpl`, and `IssuesRepositoryImpl` all return a plain `Flow<T>` that throws on backend fetch/parse failure (see `.claude/rules/data-layer.md`); `.asResult()` at the ViewModel turns that into `Result.Error`. Because these flows can throw, **every** collector must go through `.asResult()` — a bare `collect`/`launchIn` lets the exception kill the coroutine scope (`SplashViewModel`'s best-effort prefetch wraps with `.asResult()` and discards emissions for exactly this reason).

## ViewModel Layer

- Apply `.asResult()` where the UseCase `Flow` is collected, and keep the whole `Result` in `ViewModelState` (e.g. `profileResult: Result<GitHubProfile> = Result.Loading`), not just the unwrapped data. Reference: `app/feature/profile/.../destination/profile/ProfileViewModel.kt`.
- `ProfileViewModel` launches the profile, contributions, and issues loads in parallel from `init` — UseCase calls are combined in the ViewModel, never by one UseCase calling another. `SplashViewModel` fire-and-forgets the same two UseCases as a best-effort prefetch (through `.asResult()`); the repositories' `SingleFlightCache` keeps those fetches alive across navigation and never caches a failed result.
- `toState()` unwraps `Success` into the data fields (`Loading` surfaces as `null` = "no data yet") and derives failure flags from `Error` (`profileLoadFailed` / `contributionsLoadFailed` / `issuesLoadFailed`). The Profile UI renders these as per-part states — editor code skeleton, Preview building indicator, and an error row whose retry dispatches `ProfileIntent.RetryGitHubData`, which re-collects only the streams whose `Result` is `Error`.
- There is no `statusType` enum — do not introduce one.

## Prohibited Patterns

| Pattern | Alternative |
|---|---|
| `runCatching` inside a Repository `Flow` | Return plain `Flow<T>`; let `.asResult()` handle it at the ViewModel boundary |
| `kotlin.Result` in Repository/UseCase signatures | The custom `app.core.common.result.Result` at the ViewModel boundary only |
| Swallowing an exception anywhere else | Not permitted — the only documented exceptions are the discarded `asResult()` prefetch in `SplashViewModel`, the theme-persistence self-heal in `ThemeRepositoryImpl` (read/write failures fall back to the default and drop the corrupt stored pair), and the best-effort theme restore/save catches in `app:webApp` (`Main.kt` / `App.kt`) — all of which must keep coroutine cancellation intact |

See also: `.claude/rules/data-layer.md` for the Repository fetch design, `.claude/rules/usecase.md` for why UseCases stay `Result`-free, `.claude/rules/mvi-architecture.md` for `ViewModelState`/`State` shape.

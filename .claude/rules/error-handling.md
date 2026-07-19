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

Content is read-only on this portfolio site; the one write is the theme selection (`ThemeRepository.saveIsDark` — a plain `suspend fun` persisting via DataStore `edit {}`, no `Result` wrapping; the webApp caller treats it as best-effort). Do not introduce mutation-oriented `runCatching` + `onSuccess`/`onFailure` patterns without first defining a project-specific convention.

## Result Type

The custom sealed interface `Result<T>` (`Success(data)` / `Error(exception)` / `Loading`) and `Flow<T>.asResult()` live in `app/core/common/src/commonMain/kotlin/.../result/` — **not** `kotlin.Result`. `asResult()` maps emissions to `Success`, prepends `Loading` via `onStart`, and catches into `Error`.

## The One Sanctioned Fallback

`ProfileRepositoryImpl` and `ContributionsRepositoryImpl` both return a plain `Flow<T>`; they are the sole exception to **error propagation**: on backend fetch/parse failure, each falls back internally to its static snapshot (`FallbackProfile.profile` / `FallbackContributions.calendar`) instead of erroring (see `.claude/rules/data-layer.md`). Deliberate — do not "fix" it; the flows they feed are never expected to error.

## ViewModel Layer

- Apply `.asResult()` where the UseCase `Flow` is collected, and keep the whole `Result` in `ViewModelState` (e.g. `profileResult: Result<GitHubProfile> = Result.Loading`), not just the unwrapped data. Reference: `app/feature/profile/.../destination/profile/ProfileViewModel.kt`.
- `ProfileViewModel` launches the profile and contributions loads in parallel from `init` — UseCase calls are combined in the ViewModel, never by one UseCase calling another. `SplashViewModel` fire-and-forgets the same two UseCases as a best-effort prefetch; the repositories' `SingleFlightCache` keeps those fetches alive across navigation and never caches a failed result.
- `toState()` unwraps only `Success`; `Loading` and `Error` both surface as `null` in `State`. There is no error UI — `Result.Error` is retained in `ViewModelState` (for future use / debugging) but renders as "no data yet." Do not add error UI as a side effect of unrelated changes.
- There is no `statusType` enum — do not introduce one.

## Prohibited Patterns

| Pattern | Alternative |
|---|---|
| `runCatching` inside a Repository `Flow` | Return plain `Flow<T>`; let `.asResult()` handle it at the ViewModel boundary |
| `kotlin.Result` in Repository/UseCase signatures | The custom `app.core.common.result.Result` at the ViewModel boundary only |
| Swallowing an exception anywhere else | Not permitted — the repository fallback above is the **only** documented exception |

See also: `.claude/rules/data-layer.md` for the Repository fallback design, `.claude/rules/usecase.md` for why UseCases stay `Result`-free, `.claude/rules/mvi-architecture.md` for `ViewModelState`/`State` shape.

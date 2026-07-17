---
paths:
  - "core/data/**/*.kt"
  - "core/common/**/result/**/*.kt"
  - "feature/**/*ViewModel.kt"
  - "feature/**/*ViewModelState.kt"
---

# Error Handling Patterns

## Result + asResult() Layering

| Layer | Rule |
|---|---|
| Repository | Return plain `Flow<T>` — no `runCatching`, no `Result` wrapping |
| UseCase | Pass-through `Flow<T>` + `.distinctUntilChanged()` — still no `Result` wrapping |
| ViewModel | Apply `.asResult()` at the subscription point, store the whole `Result` in `ViewModelState`, handle with a `when (result)` expression |

This is a read-only portfolio site — there are no save/update/delete operations. Do not introduce mutation-oriented `runCatching` + `onSuccess`/`onFailure` patterns without first defining a project-specific convention.

## Result Type

The custom sealed interface `Result<T>` (`Success(data)` / `Error(exception)` / `Loading`) and `Flow<T>.asResult()` live in `core/common/src/commonMain/kotlin/.../result/` — **not** `kotlin.Result`. `asResult()` maps emissions to `Success`, prepends `Loading` via `onStart`, and catches into `Error`.

## The One Sanctioned Fallback

`ContributionsRepositoryImpl` still returns a plain `Flow<T>`; it is the sole exception to **error propagation**: fetch/parse failure is caught internally and replaced by the static `FallbackContributions` snapshot instead of erroring (see `.claude/rules/data-layer.md`). Deliberate — do not "fix" it; the flow it feeds is never expected to error.

## ViewModel Layer

- Apply `.asResult()` where the UseCase `Flow` is collected, and keep the whole `Result` in `ViewModelState` (e.g. `profileResult: Result<GitHubProfile> = Result.Loading`), not just the unwrapped data. Reference: `feature/profile/.../destination/profile/ProfileViewModel.kt`.
- `ProfileViewModel` chains profile → contributions loading exactly once via a private `contributionsLoadStarted` guard — UseCase calls are combined in the ViewModel, never by one UseCase calling another.
- `toState()` unwraps only `Success`; `Loading` and `Error` both surface as `null` in `State`. There is no error UI — `Result.Error` is retained in `ViewModelState` (for future use / debugging) but renders as "no data yet." Do not add error UI as a side effect of unrelated changes.
- There is no `statusType` enum — do not introduce one.

## Prohibited Patterns

| Pattern | Alternative |
|---|---|
| `runCatching` inside a Repository `Flow` | Return plain `Flow<T>`; let `.asResult()` handle it at the ViewModel boundary |
| `kotlin.Result` in Repository/UseCase signatures | The custom `core.common.result.Result` at the ViewModel boundary only |
| Swallowing an exception anywhere else | Not permitted — the contributions fallback above is the **only** documented exception |

See also: `.claude/rules/data-layer.md` for the Repository fallback design, `.claude/rules/usecase.md` for why UseCases stay `Result`-free, `.claude/rules/mvi-architecture.md` for `ViewModelState`/`State` shape.

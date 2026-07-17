---
paths:
  - "core/data/**/*.kt"
  - "core/common/**/result/**/*.kt"
  - "feature/**/*ViewModel.kt"
  - "feature/**/*ViewModelState.kt"
---

# Error Handling Patterns

## Result + asResult() Pattern

### Basic Policy

| Layer | Rule |
|---|---|
| Repository | Return plain `Flow<T>` — no `runCatching`, no `Result` wrapping (contributions is the one exception, see below) |
| UseCase | Pass-through `Flow<T>` + `.distinctUntilChanged()` — still no `Result` wrapping |
| ViewModel | Apply `.asResult()` at the subscription point, store the whole `Result` in `ViewModelState`, handle with a `when (result)` expression |

There are no save/update/delete operations in this project (it is a read-only portfolio site), so
Do not introduce mutation-oriented `runCatching` + `onSuccess`/`onFailure` patterns without first
defining a project-specific convention.

### Result Type

`core/common/src/commonMain/kotlin/.../result/Result.kt` — a custom sealed interface, **not**
`kotlin.Result`:

```kt
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}
```

### asResult() Extension

`core/common/src/commonMain/kotlin/.../result/AsResult.kt`:

```kt
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it)) }
```

## Repository Layer: the One Sanctioned Fallback

`ProfileRepository` / `ContributionsRepository` return plain `Flow<T>` with no `runCatching`.
`ContributionsRepositoryImpl` is the sole exception: it catches fetch/parse failure **internally** and
falls back to a static snapshot rather than propagating an error:

```kt
override fun getContributions(user: String): Flow<ContributionCalendar> = flow {
    val live = fetchText("$CONTRIBUTIONS_API$user?y=last")?.let(::parseContributions)
    emit(live ?: FallbackContributions.calendar)
}.flowOn(defaultDispatcher)
```

This is deliberate — see `.claude/rules/data-layer.md`. Do not "fix" it into rethrowing or emitting
`Result.Error`; the flow it feeds is never expected to error.

## ViewModel Layer

Apply `.asResult()` where the UseCase `Flow` is collected, and keep the whole `Result` in
`ViewModelState` (not just the unwrapped data).

**Example**: `feature/profile/src/commonMain/kotlin/.../destination/profile/ProfileViewModel.kt`

```kt
init {
    viewModelScope.launch {
        getProfileUseCase().asResult().collect { result ->
            when (result) {
                is Result.Success -> {
                    updateViewModelState { copy(profileResult = result) }
                    if (!contributionsLoadStarted) {
                        contributionsLoadStarted = true
                        loadContributions(result.data.handle)
                    }
                }
                is Result.Loading -> updateViewModelState { copy(profileResult = result) }
                is Result.Error -> updateViewModelState { copy(profileResult = result) }
            }
        }
    }
}
```

`ProfileViewModelState` holds `profileResult: Result<GitHubProfile> = Result.Loading` and
`contributionsResult: Result<ContributionCalendar> = Result.Loading`. A `Result.Success` on the profile
stream triggers loading contributions exactly once (guarded by a private `contributionsLoadStarted`
boolean), chaining the two UseCase calls without a UseCase depending on another UseCase.

### toState(): Only Success Is Unwrapped

`ProfileViewModelState.toState()` unwraps only the `Success` branch; `Loading` and `Error` both surface as
`null` in `State`:

```kt
profile = (profileResult as? Result.Success<GitHubProfile>)?.data,
contributions = (contributionsResult as? Result.Success<ContributionCalendar>)?.data,
```

This is the current, accepted pattern: there is no error UI. `Result.Error` is retained in
`ViewModelState` (for future use / debugging) but not surfaced to the screen — Loading and Error both
render as "no data yet," and the Preview pane simply waits until data arrives. Do not add error UI as a
side effect of unrelated changes; treat this as the established behavior.

There is no `statusType` enum — do not introduce one here.

## Prohibited Patterns

| Pattern | Alternative |
|---|---|
| `runCatching` inside a Repository `Flow` | Return plain `Flow<T>`; let `.asResult()` handle it at the ViewModel boundary |
| `kotlin.Result` in Repository/UseCase signatures | Use the custom `core.common.result.Result` at the ViewModel boundary only |
| Swallowing an exception anywhere else | Not permitted — the `ContributionsRepository` static-snapshot fallback above is the **only** documented exception |

See also: `.claude/rules/data-layer.md` for the Repository fallback design, `.claude/rules/usecase.md` for
why UseCases stay `Result`-free, `.claude/rules/mvi-architecture.md` for `ViewModelState`/`State` shape.

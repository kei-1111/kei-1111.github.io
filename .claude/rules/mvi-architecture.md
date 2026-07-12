---
paths: "app/core/mvi/**/*.kt,app/feature/**/*ViewModel.kt,app/feature/**/*State.kt,app/feature/**/*Intent.kt,app/feature/**/*Effect.kt,app/feature/**/*ViewModelState.kt"
---

# MVI Architecture Guide

This project adopts an MVI-based architecture on top of `core/mvi`.

## Core Components

| Component | Role | Marker interface |
|-----------|------|-------------------|
| `Intent` | Input that passes user actions to the ViewModel | `interface Intent` |
| `State` | Screen rendering state exposed to the UI, always carries `effect` | `interface State` |
| `ViewModelState` | Internal ViewModel state; converts to `State` via `toState()` | `interface ViewModelState<S : State> { fun toState(): S }` |
| `Effect` | One-shot side effect consumed by the UI (navigation, opening a URL) | none — a plain `sealed interface`, not a `core/mvi` type |

There is no `statusType` concept in this project. Loading/error phases are expressed with the custom `Result<T>` from `core:common` stored directly on `ViewModelState` (e.g. `profileResult: Result<GitHubProfile> = Result.Loading`) — see `.claude/rules/error-handling.md`.

---

## MviViewModel

All Destination ViewModels extend `MviViewModel<VS, S, I>`.

**Base class**: `core/mvi/src/commonMain/kotlin/io/github/kei_1111/core/mvi/MviViewModel.kt`

```kt
abstract class MviViewModel<VS : ViewModelState<S>, S : State, I : Intent> : ViewModel() {
    protected val _viewModelState = MutableStateFlow<VS>(createInitialViewModelState())
    val state: StateFlow<S> = _viewModelState
        .map(ViewModelState<S>::toState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), createInitialState())

    protected abstract fun createInitialViewModelState(): VS
    protected abstract fun createInitialState(): S
    abstract fun onIntent(intent: I)
    protected fun updateViewModelState(update: VS.() -> VS)
}
```

**Example**: `feature/profile/src/commonMain/kotlin/.../destination/profile/ProfileViewModel.kt`

### Standard ViewModel Pattern (Metro)

- Annotate the class with `@Inject`, `@ViewModelKey`, and `@ContributesIntoMap(AppScope::class, binding<ViewModel>())`, declared `internal class`
- `binding<ViewModel>()` is required because `MviViewModel<...>` is the sole declared supertype but the multibinding map expects `ViewModel`
- Constructor injects UseCases from `core:domain` only — never a Repository directly (see the layering rule in `CLAUDE.md`)
- Obtained in a navigation entry via `metroViewModel()` (`dev.zacsweers.metrox.viewmodel`), never constructed manually

```kt
@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
internal class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val getContributionsUseCase: GetContributionsUseCase,
) : MviViewModel<ProfileViewModelState, ProfileState, ProfileIntent>()
```

This project does **not** use Metro's AssistedInject pattern — no ViewModel currently takes navigation-supplied parameters (`SplashViewModel` takes none, `ProfileViewModel` only takes UseCases).

### onIntent Policy

Branch logic is written **inline** in the `when (intent)` — there are no private per-intent handler functions. Private helpers are allowed for init/observe-style flows, e.g. `ProfileViewModel`'s `private fun loadContributions(handle: String)` launched from `init {}`. `@Suppress("CyclomaticComplexMethod")` on `onIntent` is acceptable when the inline `when` grows large (both `ProfileViewModel.onIntent` and `SplashViewModel.onIntent` carry it).

---

## File Structure

Every screen defines five MVI files plus a Screen and Desktop/Mobile Content (`Xxx` = feature/destination name, e.g. `Profile`, `Splash`):

| File | Content |
|------|---------|
| `XxxViewModelState.kt` | `internal data class`, implements `ViewModelState<XxxState>`. May hold implementation detail the UI doesn't need (e.g. `contributionsResult: Result<ContributionCalendar>`). Converts via `override fun toState()`. Includes `effect: XxxEffect?` |
| `XxxState.kt` | `internal data class` (or `sealed interface` for Idle/Loading/Error phases), implements `State`. Exposed via `viewModel.state`. Also carries `effect: XxxEffect?` |
| `XxxIntent.kt` | `internal sealed interface : Intent`. Always includes a `data object ConsumeEffect` |
| `XxxEffect.kt` | `internal sealed interface`, one-shot side effects. Cleared back to `null` once handled |
| `XxxViewModel.kt` | `internal class`, extends `MviViewModel<XxxViewModelState, XxxState, XxxIntent>()` |

**Example**: `feature/profile/src/commonMain/kotlin/.../destination/profile/{ProfileViewModelState,ProfileState,ProfileIntent,ProfileEffect,ProfileViewModel}.kt`

Real shape (`feature/profile/.../ProfileIntent.kt`):

```kt
internal sealed interface ProfileIntent : Intent {
    data class UpdateLayout(val layout: WindowLayout) : ProfileIntent
    data class ToggleTree(val layout: WindowLayout) : ProfileIntent
    data class OpenUrl(val url: String) : ProfileIntent
    data object ConsumeEffect : ProfileIntent
}
```

A single-effect screen looks like `SplashEffect`: `internal sealed interface SplashEffect { data object NavigateProfile : SplashEffect }`.

See `.claude/rules/naming-conventions.md` for how individual Intent/Effect members are named.

---

## Effect Handling

Use the `MviEffect` Composable to consume an Effect — it prevents forgetting to fire `ConsumeEffect` and removes boilerplate.

**File**: `core/mvi/src/commonMain/kotlin/io/github/kei_1111/core/mvi/MviEffect.kt`

```kt
@Composable
fun <E> MviEffect(effect: E?, onConsume: () -> Unit, onHandle: (E) -> Unit)
```

`rememberUpdatedState` wraps both lambdas; when `effect != null` it runs `onHandle(effect)` then `onConsume()` inside `LaunchedEffect(effect)`.

**Example** (`feature/profile/src/commonMain/kotlin/.../destination/profile/ProfileScreen.kt`):

```kt
MviEffect(
    effect = state.effect,
    onConsume = { viewModel.onIntent(ProfileIntent.ConsumeEffect) },
) { effect ->
    when (effect) {
        is ProfileEffect.OpenUrl -> openUrl(effect.url)
    }
}
```

Never handle an Effect without also wiring `ConsumeEffect`, or it will keep re-firing on recomposition.

---

## Data Flow

1. The UI dispatches an `Intent` via `onIntent`
2. `ViewModel.onIntent` updates the internal state with `updateViewModelState { copy(...) }` (setting `effect = SomeEffect(...)` for one-shot side effects)
3. `ViewModelState.toState()` derives the public `State`; the UI recomposes off `viewModel.state`
4. `MviEffect` runs `onHandle` for a non-null `effect`
5. `MviEffect` automatically dispatches `ConsumeEffect`, clearing `effect` back to `null`

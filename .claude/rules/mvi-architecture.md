---
paths:
  - "app/core/mvi/**/*.kt"
  - "app/feature/**/*ViewModel.kt"
  - "app/feature/**/*State.kt"
  - "app/feature/**/*Intent.kt"
  - "app/feature/**/*Effect.kt"
  - "app/feature/**/*ViewModelState.kt"
---

# MVI Architecture Guide

Base types live in `app/core/mvi`: `MviViewModel<VS, S, I>`, the `Intent` / `State` / `ViewModelState<S>` marker interfaces, and the `MviEffect` composable.

## Core Components

| Component | Role |
|---|---|
| `Intent` | User action passed to the ViewModel; marker `interface Intent` |
| `State` | Screen rendering state exposed to the UI; always carries `effect`; marker `interface State` |
| `ViewModelState` | Internal ViewModel state; `interface ViewModelState<S : State> { fun toState(): S }` |
| `Effect` | One-shot side effect (navigation, opening a URL); a plain `sealed interface`, not a `app/core/mvi` type |

There is no `statusType` concept — loading/error phases are the custom `Result<T>` stored directly on `ViewModelState` (see `.claude/rules/error-handling.md`).

## ViewModel Pattern (Metro)

All destination ViewModels extend `MviViewModel<VS, S, I>` (`app/core/mvi/.../MviViewModel.kt`: `state` is derived from the internal `MutableStateFlow` via `toState()` with `WhileSubscribed(5_000)`; subclasses implement `createInitialViewModelState()` / `createInitialState()` / `onIntent` and mutate via `updateViewModelState { copy(...) }`).

- Declare `internal class`, annotated class-level `@Inject`, `@ViewModelKey`, `@ContributesIntoMap(AppScope::class, binding<ViewModel>())` — `binding<ViewModel>()` is required because `MviViewModel<...>` is the sole declared supertype but the multibinding map expects `ViewModel`.
- Constructor injects UseCases from `app:core:domain` only — never a Repository (layering rule).
- Obtained in a navigation entry via `metroViewModel()`, never constructed manually.
- No AssistedInject — no ViewModel takes navigation-supplied parameters today.

### onIntent Policy

Write branch logic **inline** in the `when (intent)` — no private per-intent handler functions. Private helpers are allowed for init/observe-style flows (e.g. `loadContributions` launched from `init {}`). `@Suppress("CyclomaticComplexMethod")` on `onIntent` is acceptable when the inline `when` grows large.

## File Structure

Five MVI files per screen, plus Screen and Desktop/Mobile Content (screen layers: `.claude/rules/ui-implementation.md`):

| File | Content |
|---|---|
| `XxxViewModelState.kt` | `internal data class`, implements `ViewModelState<XxxState>`; may hold detail the UI doesn't need; includes `effect: XxxEffect?`; converts via `toState()` |
| `XxxState.kt` | `internal data class`, implements `State`; exposed via `viewModel.state`; also carries `effect: XxxEffect?` |
| `XxxIntent.kt` | `internal sealed interface : Intent`; always includes a `data object ConsumeEffect` |
| `XxxEffect.kt` | `internal sealed interface`; cleared back to `null` once handled |
| `XxxViewModel.kt` | `internal class`, extends `MviViewModel<XxxViewModelState, XxxState, XxxIntent>()` |

Reference shapes: `app/feature/profile/.../destination/profile/` (data loading + effects) and `app/feature/splash/.../destination/splash/` (single-effect screen). Member naming: `.claude/rules/naming-conventions.md`.

## Effect Handling

Consume an Effect only through the `MviEffect` composable (`app/core/mvi/.../MviEffect.kt`): for a non-null `effect` it runs the handler inside `LaunchedEffect(effect)` and then fires `onConsume` automatically (both lambdas wrapped in `rememberUpdatedState`).

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

## Data Flow

UI dispatches an `Intent` → `ViewModel.onIntent` updates the internal state with `updateViewModelState { copy(...) }` (setting `effect = SomeEffect(...)` for one-shot side effects) → `ViewModelState.toState()` derives the public `State` and the UI recomposes → `MviEffect` handles the non-null `effect`, then automatically dispatches `ConsumeEffect`, clearing it back to `null`.

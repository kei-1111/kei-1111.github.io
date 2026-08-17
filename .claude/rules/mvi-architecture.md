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

### ViewModelState / State Split

- `ViewModelState` holds **raw values** the ViewModel owns — `Result`s, edit buffers, the open tool, selected page — and makes no display decision.
- `State` holds the **display state** the UI renders as-is. Comparing a value to an enum or to a threshold, folding several `Result`s into one phase, and deciding whether a part is shown, failed, or enabled all happen in `toState()`, never in a Compose `if`. Two Contents or Components computing the same condition is the signal to derive it there instead.
- Deriving it makes the raw flag it replaced unread — delete that field from `State` in the same change; `ViewModelState` keeps the raw value.
- A derivation the UI must parameterize (per page, per layout) is a `State` member function taking that parameter, not a field — Content is chosen by the measured width, so it cannot wait for a state echo of the layout.
- Stays in the UI: layout arithmetic (how many items fit, placeholder geometry), input-local conditions (`query.isEmpty()`), and picking what to render from a list's contents.
- Naming: `.claude/rules/naming-conventions.md` — State.

## ViewModel Pattern (Metro)

All destination ViewModels extend `MviViewModel<VS, S, I>` (`app/core/mvi/.../MviViewModel.kt`: `state` is derived from the internal `MutableStateFlow` via `toState()` with `WhileSubscribed` (params canonical in `MviViewModel.kt`); subclasses implement `createInitialViewModelState()` / `createInitialState()` / `onIntent` and mutate via `updateViewModelState { copy(...) }`).

- Declare `internal class`, annotated class-level `@Inject`, `@ViewModelKey`, `@ContributesIntoMap(AppScope::class, binding<ViewModel>())` — `binding<ViewModel>()` is required because `MviViewModel<...>` is the sole declared supertype but the multibinding map expects `ViewModel`.
- Constructor injects UseCases from `app:core:domain`, plus app-scoped cross-cutting utilities from `app:core:common` when the ViewModel needs them (e.g. `InteractionLog`) — never a Repository (layering rule).
- Obtained in a navigation entry via `metroViewModel()`, never constructed manually.
- No AssistedInject — no ViewModel takes navigation-supplied parameters today.
- Unit-tested per `.claude/rules/mvi-testing.md` (Android host tests, public-contract-only assertions).

### onIntent Policy

Write branch logic **inline** in the `when (intent)` — no private per-intent handler functions. Private helpers are allowed for init/observe-style flows (e.g. `loadContributions` launched from `init {}`). `@Suppress("CyclomaticComplexMethod")` on `onIntent` is acceptable when the inline `when` grows large. Every sealed-type `when` branch takes `is` — `data object` branches included (`is XxxIntent.ConsumeEffect ->`); enum branches stay bare (`is` does not apply to values).

Never re-dispatch another Intent from inside an `onIntent` branch (no `onIntent(OtherIntent)` calls). When two or more state-update sites (`onIntent` branches or init/observe collectors) share a state transformation, extracting it as a private function is allowed — unlike per-intent handler functions — under three constraints: the function is a pure `ViewModelState → ViewModelState` transformation — needed values arrive as immutable parameters; no reads of `_viewModelState`, injected dependencies, time/randomness, or other external mutable state, no logging, no coroutine launches, no `updateViewModelState` calls (the call site applies it inside its own `updateViewModelState { ... }`); it is a leaf function — called directly from state-update sites, never calling another private function; and it exists only while two or more sites actually use it — never as a single-site tidy-up. Purity here is correctness, not style: `updateViewModelState` delegates to `MutableStateFlow.update {}`, which may re-run the lambda on contention, so a side effect inside the transformation can execute more than once.

## File Structure

Five MVI files per screen, sitting at the `destination/<name>/` top level next to `XxxScreenRoot.kt` / `XxxScreen.kt`; Desktop/Mobile Content lives in `content/` and screen-local model types in `model/` (screen layers and directory layout: `.claude/rules/ui-implementation.md`):

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
Use the current `ProfileScreenRoot.kt` or `SplashScreenRoot.kt` as the executable reference.

Never handle an Effect without also wiring `ConsumeEffect`, or it will keep re-firing on recomposition.

## Data Flow

UI dispatches an `Intent` → `ViewModel.onIntent` updates the internal state with `updateViewModelState { copy(...) }` (setting `effect = SomeEffect(...)` for one-shot side effects) → `ViewModelState.toState()` derives the public `State` and the UI recomposes → `MviEffect` handles the non-null `effect`, then automatically dispatches `ConsumeEffect`, clearing it back to `null`.

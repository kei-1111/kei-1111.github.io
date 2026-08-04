---
paths:
  - "app/feature/**/*ViewModelTest.kt"
  - "app/feature/**/src/*Test/**/fake/**/*.kt"
  - "app/core/mvi/src/*Test/**/*.kt"
  - "app/core/testing/**/*.kt"
---

# MVI ViewModel Testing

ViewModel-specific conventions layered on `app-testing.md` (canonical for the stack, fake
policy, naming, and anti-patterns) and `tdd.md` (canonical for the test-first process).
Sibling suites: `server-testing.md`; `ui-testing.md`.

## Scope

- Unit tests for `MviViewModel` subclasses: the observable outcome of one stimulus — an Intent
  via `onIntent`, or an emission from a controllable fake boundary (a UseCase flow) — on the
  public `state` (including `effect`). Rendering and the `MviEffect` composable are out of
  scope; that is the Playwright suite's job (`ui-testing.md`).
- Run: `./gradlew :app:feature:<name>:testAndroidHostTest` — local JVM host test, see
  `app-testing.md` — Stack And Running.
- Construct the ViewModel directly with fakes (`SearchEverywhereViewModel(fake, InteractionLog())`)
  — never through Metro; the DI annotations are inert metadata in tests. App-scoped
  collaborators like `InteractionLog` are plain classes — pass a fresh instance per test
  (keep the reference to assert on it).
- Placement: `src/commonTest/kotlin`, same package as the production code, file named
  `XxxViewModelTest.kt`. detekt covers test sources automatically.

## Coroutine Setup — MUST

`viewModelScope` binds to `Dispatchers.Main` — it must be replaced with a test dispatcher
**before the ViewModel is constructed** (its `init` may already launch coroutines). Extend
`ViewModelTestBase` (`app:core:testing`, wired into every feature's `commonTest` by
`KmpFeaturePlugin`): its `@BeforeTest` / `@AfterTest` handle `Dispatchers.setMain(StandardTestDispatcher())`
/ `resetMain()` (JUnit4 rules don't exist in commonTest).

`runTest` automatically reuses the mocked Main dispatcher's scheduler, so `runCurrent()` inside
the test advances both. Write tests as expression bodies (`fun x() = runTest { ... }`) — required
for the wasmJs target that shares `commonTest`.

## Collect First, Then Intent — MUST

`MviViewModel.state` uses `WhileSubscribed(5_000)`: with no collector the `toState()` mapping
never runs and `state.value` stays frozen at the initial value. Every test that asserts on
state follows this shape:

```kotlin
@Test
fun resetsSelectionToTopOnQueryUpdate() = runTest {
    val viewModel = SearchEverywhereViewModel(FakeGetProfileUseCase(), InteractionLog())
    startCollecting(viewModel.state) // app:core:testing — collect + runCurrent, before dispatching anything

    viewModel.onIntent(SearchEverywhereIntent.UpdateQuery("README"))
    runCurrent() // flush the test dispatcher before asserting

    assertEquals(0, viewModel.state.value.selectedIndex)
}
```

- Call `runCurrent()` after every `onIntent` and every fake emission, before asserting.
- Use `advanceUntilIdle()` / `advanceTimeBy()` only when the code under test uses `delay`
  (debounce, timeouts); default to `runCurrent()`.
- To assert intermediate transitions, collect into a list with `state.toList(collected)`
  (see `MviViewModelTest.reflectsIntentDrivenUpdateIntoCollectedState`).

## Fakes

Policy (fake-only, no mocking library) and the default in-file placement: `app-testing.md`
(canonical). ViewModel specifics:

- A fake UseCase shared by multiple test classes in one feature lives in the feature's
  `commonTest` `fake/` package as `Fake{UseCaseName}` (reference:
  `app/feature/profile/src/commonTest/.../fake/FakeGetProfileUseCase.kt`).
- Flow-returning fakes use `MutableSharedFlow(replay = 1)` + a test-only `emit()`, so the test
  controls when data arrives and can observe the state before the first emission. The effective
  buffer is one item — a test emitting more than once calls `runCurrent()` between emissions so
  the collector keeps up (otherwise `emit` suspends).

## Public Contract Only — MUST

Stimulate only through `onIntent` or fake-boundary emissions; assert only through `state`.
Never touch `_viewModelState`, the `ViewModelState` type, or private helpers from a test —
tests that assert only externally meaningful outcomes stay green through behavior-preserving
internal refactors, which is what keeps the TDD refactor step safe. Private pure
transformations (`mvi-architecture.md`) are covered indirectly through the Intents that use
them.

Framework mechanics (the state-sharing strategy, coroutine plumbing) are NOT part of the
public contract even where they are observable. The one deliberate exception is
`MviViewModelTest.keepsPublicStateAtInitialValueWithoutCollector`, a characterization test
that intentionally pins `MviViewModel`'s `WhileSubscribed` behavior — not a template for
feature tests.

## Effects

Effect emission and effect consumption are two behaviors — test them separately: one test
asserts the Intent sets the expected `state.effect`; another arranges an effect and asserts
`ConsumeEffect` clears it back to `null` (reference: `SearchEverywhereViewModelTest`).

## Time-Dependent Logic

`runTest` virtualizes `delay`, but `TimeSource.Monotonic` readings do **not** follow virtual
time. New or modified ViewModel code that reads a clock MUST accept a `TimeSource` constructor
parameter defaulting to `TimeSource.Monotonic` so tests can inject `TestTimeSource` — precedent:
`server/.../util/TtlCache.kt` + `TtlCacheTest`. (`SplashViewModel` predates this rule and still reads
`TimeSource.Monotonic` directly — do not copy it; retrofit it only when asked.)

## Red → Green for a New Intent

The process itself is `tdd.md` (Canon TDD — test list, one test at a time). The VM-specific
micro-cycle for a NEW Intent subtype, whose absence makes the production `when` non-exhaustive:

1. Write the test for the new behavior (this is the compile-failure red — valid per `tdd.md`).
2. Add only the minimal contract to compile: the Intent subtype (plus any new State/Effect
   field) and a no-op `when` branch.
3. Run and observe the assertion failure — the meaningful red.
4. Implement the minimal branch logic (green), then refactor with everything green.

For a new destination, scaffold only compilable defaults (`create-destination` skill), then add
branches one behavior at a time — do not design several behaviors ahead of their first red test.

## Future Considerations

- Running `wasmJsTest` in CI for distribution-target parity — never the TDD loop (browser
  startup is too slow).
- Turbine: revisit only if hand-rolled collectors stop scaling.

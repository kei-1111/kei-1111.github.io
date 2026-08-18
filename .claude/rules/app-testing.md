---
paths:
  - "app/**/src/commonTest/**"
  - "shared/model/src/commonTest/**"
---

# App Unit Testing

Sibling suites: `server-testing.md`; `ui-testing.md`. New logic is written test-first per
`tdd.md`.

## Test Doubles: Hand-Written Fakes Only

No mocking library is used and none may be added — hand-written fakes are the officially
preferred double ([Use test doubles in Android](https://developer.android.com/training/testing/fundamentals/test-doubles)).

- A fake is a private class (or anonymous object) in the test file implementing the
  dependency's interface and returning canned values (e.g. `flowOf(...)`). A fake shared by
  multiple test classes within one feature lives in that feature's `commonTest` `fake/`
  package instead (`mvi-testing.md` — Fakes).
- A fake complex enough to need its own tests is a design smell in the code under test — do
  not reach for a mock.

## Structure And Naming

- Arrange-Act-Assert, separated by blank lines.
- Test names are camelCase sentences describing the behavior
  (`collapsesConsecutiveDuplicateEmissions`), shared with the server suite. No backtick names —
  they are runtime-restricted
  ([Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)); one
  convention keeps every suite uniform.
- One cohesive behavior per test: several assertions on one resulting object are fine;
  unrelated verifications bolted into one test are not.

## What To Test Per Layer

- **Shared helpers** (`app/core/common/src/commonTest/`): exercise the helper's observable
  contract directly — for the suppression helpers that means recovery, cancellation
  propagation, and catch-type selectivity. Reference: `SuppressionTest.kt`.
- **Api client** (`app/core/api/src/commonTest/`): construct the `internal` `...Impl` against
  `HttpClient(MockEngine)` + ContentNegotiation JSON and assert the fold-to-null contract
  (success decode + requested URL, non-200 / thrown failure → null). The shared `getOrNull`
  paths (malformed body, cancellation propagation) are pinned once in `ProfileApiImplTest`,
  not repeated per Api. Reference: `ProfileApiImplTest.kt`.
- **Repository** (`app/core/data/src/commonTest/`): construct the `internal` `...Impl`
  directly against a fake of its Api/DataSource interface and assert observable behavior
  (delegation, default resolution). Reference: `ThemeRepositoryImplTest.kt`.
- **Local data source** (`app/core/local/src/commonTest/`): construct the `internal` `...Impl`
  via its primary constructor against hand-written `DataStore<Preferences>` fakes (the DI
  seam: `.claude/rules/data-layer.md`) and assert observable read/write behavior including
  the corruption-recovery paths. Reference: `ThemeLocalDataSourceImplTest.kt`.
- **UseCase** (`app/core/domain/src/commonTest/`): construct the `internal` `...Impl`
  directly against a fake Repository. Every `Get`-style UseCase test covers both forwarding
  and the `.distinctUntilChanged()` collapsing required by `.claude/rules/usecase.md`
  (`[a, a, b, a]` must come out `[a, b, a]`). Use the current tests in that directory as the
  executable references.
- **ViewModel** (`app/feature/<name>/src/commonTest/` and the `MviViewModel` base in
  `app/core/mvi`): stimulate through `onIntent` or a fake-boundary emission and assert the
  observable `State` / `Effect` outcomes — never internal calls. Coroutine setup, the
  collect-first rule, and the other ViewModel-specific conventions: `mvi-testing.md`
  (canonical). Composable rendering belongs to `ui-testing.md` (Playwright).
- Do not test the dependency's own implementation, the Kotlin stdlib, or coroutines library
  behavior.

## Anti-Patterns (Prohibited)

Mocking libraries / over-mocking; asserting implementation details instead of observable
behavior; unrelated assertions piled into one test; backtick test names. Process-level
anti-patterns: `tdd.md`.

## Stack And Running

`kotlin-test` + `kotlinx-coroutines-test` — `runTest {}` with `toList()` for finite cold
flows; no `TestDispatcher` needed there, and Turbine is deliberately not a dependency.
Shared test infrastructure (`ViewModelTestBase`, `startCollecting`) lives in
`app:core:testing`, wired into every feature's `commonTest` by `KmpFeaturePlugin`.
Tests run on the non-shipped Android target as host tests — local JVM, no emulator, no
Robolectric (wiring: `.claude/rules/gradle.md` — Convention Plugins). The command selection is
canonical in `.claude/rules/working-agreement.md` — Build And Validation,
and the CI module set in `.github/workflows/app-test.yml`.
The JVM-only conventions suite mirrors written rules and runs with `./gradlew :test:conventions:test`.

`shared/model` is the exception — host tests are not enabled there. Its required tasks, including
the server-side wire contract, are canonical in `.claude/rules/working-agreement.md` — Build And
Validation; target wiring is executable in `.github/workflows/shared-test.yml` and
`.github/workflows/server-test.yml`.

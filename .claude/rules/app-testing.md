---
paths:
  - "app/**/src/commonTest/**"
  - "app/core/domain/**/*.kt"
---

# App Unit Testing

Sibling suites: `server-testing.md`; `ui-testing.md`. New logic is written test-first per
`tdd.md`.

## Test Doubles: Hand-Written Fakes Only

No mocking library is used and none may be added — hand-written fakes are the officially
preferred double ([Use test doubles in Android](https://developer.android.com/training/testing/fundamentals/test-doubles)).

- A fake is a private class (or anonymous object) in the test file implementing the
  dependency's interface and returning canned values (e.g. `flowOf(...)`).
- A fake complex enough to need its own tests is a design smell in the code under test — do
  not reach for a mock.

## Structure And Naming

- Arrange-Act-Assert, separated by blank lines.
- Test names are camelCase sentences describing the behavior
  (`collapsesConsecutiveDuplicateEmissions`), shared with `:server:test`. No backtick names —
  they are runtime-restricted
  ([Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)); one
  convention keeps every suite uniform.
- One cohesive behavior per test: several assertions on one resulting object are fine;
  unrelated verifications bolted into one test are not.

## What To Test Per Layer

- **UseCase** (`app/core/domain/src/commonTest/`): construct the `internal` `...Impl`
  directly against a fake Repository. Every `Get`-style UseCase test covers both forwarding
  and the `.distinctUntilChanged()` collapsing required by `.claude/rules/usecase.md`
  (`[a, a, b, a]` must come out `[a, b, a]`). Reference: `GetProfileUseCaseTest.kt`,
  `GetContributionsUseCaseTest.kt`, `GetLicensesUseCaseTest.kt`.
- **ViewModel** (`app/feature/<name>/src/commonTest/`, once the first lands): drive Intents
  through a fake UseCase and assert the observable `State` / `Effect` outcomes — never
  internal calls. Composable rendering belongs to `ui-testing.md` (Playwright).
- Do not test the dependency's own implementation, the Kotlin stdlib, or coroutines library
  behavior.

## Anti-Patterns (Prohibited)

Mocking libraries / over-mocking; asserting implementation details instead of observable
behavior; unrelated assertions piled into one test; backtick test names. Process-level
anti-patterns: `tdd.md`.

## Stack And Running

`kotlin-test` + `kotlinx-coroutines-test` — `runTest {}` with `toList()` for finite cold
flows; no `TestDispatcher` needed there, and Turbine is deliberately not a dependency.

```bash
./gradlew :app:core:domain:wasmJsBrowserTest
```

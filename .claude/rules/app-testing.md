---
paths:
  - "app/**/src/commonTest/**"
  - "app/core/domain/**/*.kt"
---

# App Unit Testing

Sibling suites: `server-testing.md`; `ui-testing.md`. New logic is written test-first per
`tdd.md`.

## Test Doubles: Hand-Written Fakes Only

No mocking library is used and none may be added — fakes are the officially preferred double
([Use test doubles in Android](https://developer.android.com/training/testing/fundamentals/test-doubles):
"Don't require a mocking framework and are lightweight. They are preferred.").

- A fake is a private class (or anonymous object) in the test file implementing the
  dependency's interface and returning canned values (e.g. `flowOf(...)`).
- If a fake grows complex enough to need its own tests, treat that as a design smell in the
  code under test — do not reach for a mock.

## Structure And Naming

- Arrange-Act-Assert, separated by blank lines.
- Test names are camelCase sentences describing the behavior
  (`collapsesConsecutiveDuplicateEmissions`) — the repo-wide convention shared with
  `:server:test`. Backtick names with spaces are not used on any target: the
  [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) note they
  are runtime-restricted (e.g. Android API 30+), so one camelCase convention keeps every
  suite portable and uniform.
- One cohesive behavior per test: several assertions on one resulting object are fine;
  unrelated verifications bolted into one test are not.

## What To Test Per Layer

- **UseCase** (`app/core/domain/src/commonTest/`): construct the `internal` `...Impl` directly
  against a fake Repository. Every `Get`-style UseCase test covers both forwarding and the
  `.distinctUntilChanged()` collapsing required by `.claude/rules/usecase.md` (a fake flow
  emitting `[a, a, b, a]` must come out `[a, b, a]` — consecutive duplicates collapse, a
  re-emission after a different value survives). Reference: `GetProfileUseCaseTest.kt`,
  `GetContributionsUseCaseTest.kt`, `GetLicensesUseCaseTest.kt`.
- **ViewModel** (`app/feature/<name>/src/commonTest/`, once the first lands): drive Intents
  through a fake UseCase and assert the observable `State` / `Effect` outcomes — never
  internal calls. Composable rendering belongs to `ui-testing.md` (Playwright), not here.
- Do not test the dependency's own implementation (the Repository has its own layer), the
  Kotlin stdlib, or coroutines library behavior.

## Anti-Patterns (Prohibited)

Mocking libraries / over-mocking; asserting implementation details instead of observable
behavior; unrelated assertions piled into one test; backtick test names. Process-level
anti-patterns (test-after, tautological tests): `tdd.md`.

## Stack And Running

`kotlin-test` + `kotlinx-coroutines-test` — `runTest {}` with `toList()` for finite cold
flows (no `TestDispatcher` injection needed there; Turbine is deliberately not a dependency).

```bash
./gradlew :app:core:domain:wasmJsBrowserTest
```

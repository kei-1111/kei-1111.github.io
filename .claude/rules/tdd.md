---
paths:
  - "app/core/domain/**/*.kt"
  - "app/core/data/**/*.kt"
  - "app/feature/**/*ViewModel.kt"
  - "app/**/src/commonTest/**"
  - "server/src/**/*.kt"
---

# TDD Process

The development process for new logic across the codebase, on both the client and `:server`.
Today that means UseCase / ViewModel logic and the server's routing / service / client /
cache; as unit-test coverage extends to further layers (Repository is planned next), the same
process applies there — this rule is layer-agnostic. Suite-specific conventions — stack,
fakes, naming, what to test — live in `app-testing.md` for the client and
`server-testing.md` for the server (`ui-testing.md` covers the Playwright E2E suite, which
stays outside the inner red-green cycle — see Optional Outer Loop below).

## The Cycle ([Canon TDD](https://newsletter.kentbeck.com/p/canon-tdd))

1. Write a test list first: the expected behaviors (including edge cases) as a plain list,
   without implementing any of them. Behaviors discovered while working go back onto the
   list, not straight into code.
2. Turn exactly ONE list item into a concrete test and run it — write only enough of the
   test to produce the next failure (a compile failure is a valid first red). The failure
   must be observed AND be for the intended behavioral reason — a broken fixture, timeout,
   or unrelated suite failure does not count as red — before any production code is written.
3. Write the minimum production code that makes it (and all previous tests) pass (green).
4. Refactor if needed, keeping everything green. Repeat from step 2 until the list is empty.

- One test at a time — do not convert the whole test list into test code up front and then
  implement.
- Writing tests after the implementation merely to confirm it is NOT TDD and defeats the
  guardrail purpose of this rule (a test written after the fact tends to assert whatever the
  code already does).
- Do not retroactively backfill tests for pre-existing code as a side effect of an unrelated
  change.

## Optional Outer Loop (E2E Acceptance Test First)

For a feature with a clear user-visible behavior (a testTag-addressable interaction), a
Playwright E2E test MAY be written first as an acceptance test — double-loop TDD: the outer
E2E test stays red while the feature is built through the inner unit-level cycle above, and
turns green when the feature is complete. Run it only at feature start and feature end — the
production-build harness (`ui-testing.md` — Running) is far too slow for the inner cycle.
Visual appearance (layout, colors) has no test-first assertion and stays judged by eye; the
outer loop covers behavior only. The outer loop is optional; the inner cycle is not.

## Process Anti-Patterns (Prohibited)

Test-after masquerading as TDD; tautological tests (deriving the expected value with the
same logic as the implementation). Suite-level anti-patterns (over-mocking, implementation
details, naming): `app-testing.md` / `server-testing.md`.

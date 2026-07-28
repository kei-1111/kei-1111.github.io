---
paths:
  - "app/core/domain/**/*.kt"
  - "app/core/data/**/*.kt"
  - "app/feature/**/*ViewModel.kt"
  - "app/**/src/commonTest/**"
  - "server/src/**/*.kt"
---

# TDD Process

New logic on both the client and `:server` is developed test-first; the rule is
layer-agnostic. In a layer whose suite does not exist yet (Repository today), the new logic
introduces the `commonTest` coverage itself, and its conventions land in `app-testing.md`
with that first test. Suite conventions: `app-testing.md` (client) / `server-testing.md`
(server). The Playwright E2E suite (`ui-testing.md`) stays outside the inner cycle — see
Optional Outer Loop. The step-by-step execution workflow for implementation work is the `tdd`
skill.

## The Cycle ([Canon TDD](https://newsletter.kentbeck.com/p/canon-tdd))

1. Write a test list: the expected behaviors (including edge cases), implementing none of
   them. Behaviors discovered while working go onto the list, not straight into code.
2. Turn exactly ONE list item into a test and run it — write only enough test code to
   produce the next failure (a compile failure counts). Observe the red and confirm it fails
   for the intended behavioral reason (not a broken fixture, timeout, or unrelated failure)
   before writing any production code.
3. Write the minimum production code that makes it (and all previous tests) pass.
4. Refactor, keeping everything green. Repeat from step 2 until the list is empty.

- Do not convert the whole list into test code up front.
- Tests written after the implementation to confirm it are not TDD.
- Do not retroactively backfill tests for pre-existing code as a side effect of an
  unrelated change.

## Optional Outer Loop (E2E Acceptance Test First)

For a feature with clear user-visible behavior (a testTag-addressable interaction), a
Playwright E2E test MAY be written first as its acceptance test (double-loop TDD). The
outer loop is optional; the inner cycle is not.

- Confirm at feature start that it fails because the behavior is absent — not a broken
  harness, fixture, or locator — then leave it red until the feature completes.
- Run it only at feature start and feature end; the production-build harness
  (`ui-testing.md` — Running) is too slow for the inner cycle.
- Behavior only — visual appearance has no test-first assertion and stays judged by eye.

## Process Anti-Patterns (Prohibited)

Test-after masquerading as TDD; tautological tests (expected value derived with the same
logic as the implementation). Suite-level anti-patterns: `app-testing.md` /
`server-testing.md`.

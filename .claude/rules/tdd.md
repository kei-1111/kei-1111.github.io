---
paths:
  - "app/core/domain/**/*.kt"
  - "app/core/data/**/*.kt"
  - "app/core/api/**/*.kt"
  - "app/core/common/**/*.kt"
  - "app/core/local/**/*.kt"
  - "app/core/mvi/**/*.kt"
  - "app/feature/**/*ViewModel.kt"
  - "app/**/src/commonTest/**"
  - "shared/model/**/*.kt"
  - "server/src/**/*.kt"
---

# TDD Process

New logic on both the client and `:server` is developed test-first; the rule is
layer-agnostic. In a layer whose suite does not exist yet, the new logic introduces the
`commonTest` coverage itself, and its conventions land in `app-testing.md` with that first
test. Suite conventions: `app-testing.md` (client) / `server-testing.md`
(server). The Playwright E2E suite (`ui-testing.md`) stays outside the inner cycle — see
Optional Outer Loop.

This rule owns what TDD applies to and what is prohibited. The cycle in short: write a test
list; one item at a time — red (confirm it fails for the intended behavioral reason), minimum
green, refactor kept green — until the list is empty. The step-by-step execution
([Canon TDD](https://newsletter.kentbeck.com/p/canon-tdd)) is canonical in the `tdd` skill.

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

Converting the whole test list into test code up front; test-after masquerading as TDD;
tautological tests (expected value derived with the same logic as the implementation);
retroactively backfilling tests for pre-existing code as a side effect of an unrelated
change; editing a test to make a wrong implementation pass — the production code is what
changes, and a test is revised only when its expectation is genuinely wrong (say so).
Suite-level anti-patterns: `app-testing.md` / `server-testing.md`.

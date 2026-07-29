---
name: tdd
description: Drive implementation of new or changed logic through the red-green-refactor TDD cycle — one failing test at a time, minimal code to green, refactor kept green. Use when implementing logic in a testable layer (UseCase, ViewModel, Repository, :server), whether the user asks for TDD directly or the work arrives via implement-issue, so tests are written first instead of after the fact.
---

# TDD

## Task overview

Execute the project's test-first process as a concrete workflow. The process itself is canonical
in the TDD rule (`tdd.md`, reachable from `AGENTS.md`), and the suite conventions for the touched
layer are `app-testing.md` / `mvi-testing.md` / `server-testing.md`. This skill only sequences
them — where it and a rule disagree, the rule wins.

## Preconditions

- The change adds or modifies logic in a testable layer: UseCase, ViewModel (reducer logic),
  Repository, or `:server`. Pure UI rendering has no test-first assertion — Playwright E2E and
  visual judgment cover it (`ui-testing.md`).
- The test can actually be written first. In a layer whose suite does not exist yet
  (Repository today), the first test introduces the suite itself per the TDD rule — that is
  not a blocker. If the test still cannot be written first (an untestable seam, missing
  infrastructure with no bootstrap path), STOP and surface that as the finding instead of
  silently skipping TDD.

## Workflow

1. **Test list** — from the requirement, write the list of expected behaviors including edge
   cases, implementing none of them. Behaviors discovered mid-cycle go onto the list, not
   straight into code.
2. **Red** — turn exactly ONE list item into a test (conventions per the suite rule for the
   layer). Run the narrowest test task and observe the failure; confirm it fails for the
   intended behavioral reason — a compile failure counts, a broken fixture or unrelated error
   does not.
3. **Green** — write the minimum production code that makes the new test and all previous
   tests pass; re-run the same task.
4. **Refactor** — clean up with everything kept green; re-run after refactoring.
5. Repeat 2–4 until the test list is empty.
6. **Final validation** — the usual project validation: `./gradlew detekt` (rerun once if
   autoCorrect reformats) plus the narrowest compile of the distribution target
   (e.g. `./gradlew :app:feature:<name>:compileKotlinWasmJs`).

Narrowest test tasks:

| Layer | Task |
|---|---|
| UseCase | `./gradlew :app:core:domain:testAndroidHostTest` |
| ViewModel | `./gradlew :app:feature:<name>:testAndroidHostTest` (base class: `:app:core:mvi:testAndroidHostTest`) |
| Server | `./gradlew :server:test` |
| Repository | No task yet — the first test bootstraps the `commonTest` suite (see the TDD rule) |

## Guardrails

- Never edit a test to make a wrong implementation pass. When red persists, the production code
  is what changes; revise the test only if its expectation is genuinely wrong, and say so.
- Do not convert the whole test list into test code up front, and do not write tests after the
  implementation and present the result as TDD.
- No tautological tests — never derive the expected value with the same logic as the
  implementation.
- Do not retroactively backfill tests for pre-existing code as a side effect of the change.

## Model routing

Test authoring and the red/green judgment (why a test fails, whether green is genuine) stay with
the directing agent. Where the product has a delegated implementation lane (see the project
guide's model routing), the Green step may be delegated with the failing test as the contract —
the delegate makes the test pass and never edits it.

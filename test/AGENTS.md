# AGENTS.md — test/

Rules for the E2E infrastructure (`test/tags`, `test/e2e`). The root `AGENTS.md` still applies.
Detailed conventions live in the canonical rules below; keep this file limited to test-scoped
invariants that are useful at the entry point.

## Canonical Rules

- Suite scope, interaction conventions, and running: `.claude/rules/ui-testing.md`
- Tag naming and definition: `.claude/rules/naming-conventions.md` — testTag

## Test-Scoped Invariants

- `test/tags` holds the single definition of every tag string; app composables and Playwright
  locators both resolve it from there.

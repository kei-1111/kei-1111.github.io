# AGENTS.md — test/

Rules for the E2E infrastructure (`test/tags`, `test/e2e`). The root `AGENTS.md` still applies; this file adds the test-specific rules.

- Canonical detail: `.claude/rules/ui-testing.md` and `.claude/rules/naming-conventions.md` —
  testTag.
- `test/tags` holds the `TestTags` constants shared between `Modifier.testTag(...)` in `app/feature/*` and the Playwright locators — never inline the literal on either side.
- Run and compile the suite exactly as documented in `.claude/rules/ui-testing.md` — Running and
  `.claude/rules/working-agreement.md` — Build And Validation.

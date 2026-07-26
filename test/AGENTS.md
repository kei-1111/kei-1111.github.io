# AGENTS.md — test/

Rules for the E2E infrastructure (`test/tags`, `test/e2e`). The root `AGENTS.md` still applies; this file adds the test-specific rules.

- Canonical detail: `.claude/rules/ui-testing.md` (Playwright/Chromium conventions, canvas interaction, Page Objects, scope) and `.claude/rules/naming-conventions.md` — testTag (tag naming and placement).
- `test/tags` holds the `TestTags` constants shared between `Modifier.testTag(...)` in `app/feature/*` and the Playwright locators — never inline the literal on either side.
- `:test:e2e` runs only with `-PbaseUrl=...` against a served `:app:webApp:wasmJsBrowserDistribution` and never as part of `check`/`build`; compile-validate with `./gradlew :test:e2e:compileTestKotlin`.

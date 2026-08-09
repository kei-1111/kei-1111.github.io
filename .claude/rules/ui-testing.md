---
paths:
  - "test/**"
  - "app/feature/**/src/commonMain/**/component/**/*.kt"
  - "app/feature/**/src/commonMain/**/content/**/*.kt"
  - "app/feature/**/src/commonMain/**/*Screen*.kt"
  - "app/feature/**/src/commonMain/**/*Dialog*.kt"
---

# UI Testing (Playwright)

Sibling suites: `server-testing.md` (Ktor server); `app-testing.md` (client unit tests;
ViewModel specifics in `mvi-testing.md`). E2E tests sit outside the inner TDD cycle, but for a
feature with clear user-visible behavior one may be written first as an acceptance test
(`tdd.md` — Optional Outer Loop).

## Scope — UI tests, not full E2E

`:test:e2e` drives a statically served built wasm client in a real Chromium via Playwright.
Despite the module name, these tests verify
**client UI behavior only** — server connectivity is NOT covered. When the API is unreachable the
app still renders the IDE shell, with the server-delivered parts (GitHub data and the README
page) showing loading/error states, so tests must not assert on live server data; server behavior
is covered by the server test suite.

## Writing a Test

- Subclass `PlaywrightTestBase`: it launches Chromium once per class, opens a fresh
  context/page per test carrying `baseURL` and a pinned `ja-JP` locale (the app's display
  language follows the browser locale), and waits out the Splash → Profile transition —
  a test body contains only interactions and assertions. Override `viewport` for a cold-start
  window size (e.g. below the window-layout breakpoint — `WindowLayout.kt`) and `configurePage` for pre-navigation setup
  (e.g. `page.route(...)` to force a deterministic fetch failure — never rely on the production
  API being unreachable).
- Page Objects live in `test/e2e/.../page/` (e.g. `SplashPage`, `ProfilePage`,
  `SearchEverywherePage`).
- Locate elements with `page.locator("#${TestTags.<Feature>.<TAG>}")` — the tag value is the DOM
  `id`. Tag naming and the single-source-constant rule: `.claude/rules/naming-conventions.md` —
  testTag (canonical home).

## Interaction Conventions

- Click with `dispatchEvent("click")`, never `.click()` — the `<canvas>` overlays the a11y mirror
  and intercepts real pointer events; a synthetic DOM click reaches the CMP listener the way a
  screen reader does. Caveat: this path fires even on a `clickable(enabled = false)` node
  (verified), so assert disabled behavior with a real pointer click (`page.mouse().click(x, y)`)
  at the element's coordinates instead.
- Keep Playwright's `testIdAttribute` at its default and select CMP nodes by `#id`.
  `contentDescription` (surfaced as `aria-label`) is accessibility-only: locate and assert via
  `TestTags` exclusively and never hardcode label wording (no `getByLabel` / text locators on
  UI strings). Canvas-rendered state with no other DOM signal (e.g. theme) is observed as an
  opaque before/after change of an attribute on the testTag-located element
  (`ProfilePage.themeState()`).
- Dialog destinations use `InlineDialogSceneStrategy`, so dismissal must leave the root a11y mirror
  visible and operable. Assert the actual post-dismiss DOM state with `isVisible` and continue
  interacting through the page object; do not substitute pixel comparisons, persisted state, or
  element-count-only assertions for this regression.

## testTag Placement

- The purpose-specific composable owns its tag (e.g. `ThemeToggleButton` applies it internally);
  shared primitives never hardcode a tag — they only relay the `modifier` they receive.
- Apply the tag to the interactive node (the one carrying `clickable` / `onClick`), not the
  container or the inner icon — CMP attaches both the `id` and the click listener there.
- Do not nest one clickable semantics node inside another: DOM click events bubble and can invoke
  both actions. Model controls such as a tab and its close button as sibling interactive nodes.
- Conditionally compose an interactive node with its click action already present. With the
  catalog-resolved Compose Web runtime, adding `OnClick` to an existing semantics node does not
  attach a DOM click listener.

## Running

For infrastructure changes that do not alter browser behavior, compile the suite without serving
the app:

```bash
./gradlew :test:e2e:compileTestKotlin
```

Default local loop — the development build skips the production optimization step:

```bash
./gradlew :app:webApp:wasmJsBrowserDevelopmentExecutableDistribution
# Serve the output statically, e.g.:
python3 -m http.server 8083 --directory app/webApp/build/dist/wasmJs/developmentExecutable
./gradlew :test:e2e:test -PbaseUrl=http://localhost:8083 --tests "*SearchEverywhereE2eTest"  # skipped entirely without -PbaseUrl
```

- Scope day-to-day runs with `--tests`; run the full suite only for cross-cutting changes.
- The test task deliberately disables Gradle up-to-date skipping and build-cache reuse because the
  served application is external state; do not re-enable either optimization.
- Before trusting results, confirm the served build is yours — a parallel session may already
  occupy the port with a stale build: `curl -s localhost:8083/<hash>.wasm | grep -q <a testTag>`;
  move to a free port if taken.
- The development build is a different binary from production: PR CI (`ui-test.yml`, docs-only
  gated) runs this same development-build flow, and the production binary is E2E-gated in
  `deploy-app.yml` before the Pages deploy. To reproduce that locally, build
  the production client using `.claude/rules/gradle.md` — Development And Packaging Commands,
  serve it with the `verify-app` skill's procedure, and run the suite against that origin. Remove
  the example class filter when reproducing the full deploy gate.
- For interactive (non-suite) verification of the built app in a headless browser, use the
  `verify-app` skill.

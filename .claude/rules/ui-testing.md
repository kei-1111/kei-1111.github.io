---
paths:
  - "test/**"
---

# UI Testing (Playwright)

Sibling suites: `server-testing.md`; `mvi-testing.md` once the planned ViewModel unit tests land.

## Scope — UI tests, not full E2E

`:test:e2e` drives the built wasm client (the `:app:webApp:wasmJsBrowserDistribution` output,
served statically) in a real Chromium via Playwright. Despite the module name, these tests verify
**client UI behavior only** — server connectivity is NOT covered. The app silently renders the
client-side `FallbackProfile` (`app:core:data`) when the API is unreachable, so tests must not
assert on live server data; server behavior is covered by `:server:test`.

## Writing a Test

- Subclass `PlaywrightTestBase`: it launches Chromium once per class, opens a fresh
  context/page per test carrying `baseURL`, and waits out the Splash → Profile transition —
  a test body contains only interactions and assertions.
- Page Objects live in `test/e2e/.../page/` (e.g. `SplashPage`).
- Locate elements with `page.locator("#${TestTags.<Feature>.<TAG>}")` — the tag value is the DOM
  `id`. Tag naming and the single-source-constant rule: `.claude/rules/naming-conventions.md` —
  testTag (canonical home).

## Interaction Conventions

- Click with `dispatchEvent("click")`, never `.click()` — the `<canvas>` overlays the a11y mirror
  and intercepts real pointer events; a synthetic DOM click reaches the CMP listener the way a
  screen reader does.
- Keep Playwright's `testIdAttribute` at its default and select CMP nodes by `#id`. Assertions may
  use `getByLabel` / `getByRole` where the element exposes `aria-label` / `role`.

## testTag Placement

- The purpose-specific composable owns its tag (e.g. `ThemeToggleButton` applies it internally);
  shared primitives never hardcode a tag — they only relay the `modifier` they receive.
- Apply the tag to the interactive node (the one carrying `clickable` / `onClick`), not the
  container or the inner icon — CMP attaches both the `id` and the click listener there.

## Running

```bash
./gradlew :app:webApp:wasmJsBrowserDistribution
# Serve the output statically, e.g.:
python3 -m http.server 8083 --directory app/webApp/build/dist/wasmJs/productionExecutable
./gradlew :test:e2e:test -PbaseUrl=http://localhost:8083  # skipped entirely without -PbaseUrl
```

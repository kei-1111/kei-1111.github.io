---
name: verify-app
description: "Drive the built wasm app in a headless Playwright browser to verify UI behavior interactively during development — build and serve the distribution, wait out the Splash → Profile transition, click elements by testTag id, and take screenshots for visual checks. Use when asked to run the app, verify or demo a change in the real app, take app screenshots (動作確認・実際に動かして確認), as opposed to running the automated :test:e2e suite."
---

# Verify app

## Task overview

Interactively drive the built wasm client in a headless browser to confirm a change's real
behavior — the manual complement to the automated `:test:e2e` suite (whose conventions live in
`ui-testing.md`, reachable from `AGENTS.md`, and apply here too). The app renders to a single
`<canvas>`, so naïve browser automation fails in specific ways; this runbook encodes the working
procedure.

**Headless-only policy: never drive the user's real Chrome.** Always launch a headless
Playwright browser; close it when done so no window or instance lingers.

## Workflow

1. **Build and serve** the distribution statically
2. **Navigate and wait** for the Splash → Profile transition
3. **Interact** through testTag ids in the a11y mirror
4. **Verify visually** with screenshots
5. **Clean up** — close the browser, stop the server, remove stray output

## Build and serve

```bash
./gradlew :app:webApp:wasmJsBrowserDistribution
python3 -m http.server 8080 -d app/webApp/build/dist/wasmJs/productionExecutable
```

- Navigate to `http://localhost:8080` — never `127.0.0.1`. The server's CORS allowlist
  (`server/.../plugins/Cors.kt`) compares the origin as a literal host string, so only
  `localhost:8080` receives live API data; anything else looks like a client bug but is CORS.
  On other ports/hosts the app still renders the IDE shell with the GitHub-data parts in
  loading/error states — acceptable for UI-only checks.
- Before serving, check the port is free: `lsof -nP -iTCP:8080 -sTCP:LISTEN`. A parallel
  session may already be serving a stale build there. When elements you just added aren't
  found, confirm the served binary is yours before suspecting the code:
  `curl -s localhost:8080/<hash>.wasm | grep -ac <a new testTag value>` (expect ≥ 1 — tag
  strings live in the `.wasm`, not `webApp.js`). If the port is taken, move to a free one.
- Do **not** reuse a running dev server (`wasmJsBrowserDevelopmentRun`): it serves a snapshot
  of its startup build (later source edits are not picked up, even with `--continuous`), and
  automation tooling that writes logs under the repo root makes
  webpack live-reload the page in an endless loop.
- Freeze source edits while verifying — any rebuild or reload sends the app back to Splash and
  invalidates in-memory state. If you must edit, rebuild and restart the verification.

## Navigate and wait

Wait out the Splash → Profile transition before any interaction — input on Splash is ignored
by design. Poll for a stable Profile element instead of sleeping, e.g. wait until
`#profile-title-bar-theme-toggle` (`TestTags.Profile.TITLE_BAR_THEME_TOGGLE`) is visible. The
display language follows the browser locale (the `:test:e2e` suite pins `ja-JP`), so pin the
context's locale before asserting on visible text.

## Interact

- The canvas mirrors interactable nodes into a hidden a11y DOM (inside a shadow root —
  Playwright `#id` locators pierce it automatically). The DOM `id` is the testTag value; the
  constants live in `test/tags/.../TestTags.kt`.
- Click with `locator.dispatchEvent("click")`, never a real pointer click — the `<canvas>`
  overlays the mirror and intercepts real pointer events. Caveat: the synthetic path fires
  even on a `clickable(enabled = false)` node, so verify disabled behavior with a real
  pointer click at the element's coordinates (`page.mouse().click(x, y)` in the JVM API,
  `page.mouse.click(x, y)` when evaluating JS).
- Fall back to coordinate clicks only for untagged elements: take a screenshot, derive the
  coordinates, then a mouse coordinate click. Keyboard input goes through `page.keyboard` as
  usual.
- Some automation bridges do not expose Node globals (`setTimeout`, `Buffer`) inside
  evaluated snippets — use `page.waitForTimeout(...)` and do file comparisons in the shell.

## Verify visually

- A screenshot can lag one frame behind the real state. Before concluding "the change is not
  reflected" or "nothing happened", take a second screenshot of the same state.
- Read the screenshot file and inspect it; resize the viewport to check responsive
  breakpoints (e.g. below the 900dp compact breakpoint).
- Keep screenshots out of the repository: save under an untracked directory and delete them
  when done — never leave PNGs in the repo root.

## Clean up

Close the browser, stop the static server by killing the PID you started — never kill by port
blindly: after a port move, another session's server may be the one holding 8080 — and remove
any automation output left under the repo root.

---
name: verify-app
description: "Drive the built wasm app in a headless Playwright browser to verify UI behavior interactively during development — build and serve the distribution, wait out the Splash → Profile transition, click elements by testTag id, and take screenshots for visual checks. Use when asked to run the app, verify or demo a change in the real app, take app screenshots (動作確認・実際に動かして確認), as opposed to running the automated :test:e2e suite."
user-invocable: false
---

# Verify app

## Task overview

Interactively drive the built wasm client in a headless browser to confirm a change's real
behavior — the manual complement to the automated `:test:e2e` suite (whose conventions live in
`.claude/rules/ui-testing.md`, and apply here too). The app renders to a single
`<canvas>`, so naïve browser automation fails in specific ways; this runbook encodes the working
procedure.

**Headless-only policy: never drive the user's real Chrome.** Always launch a headless
Playwright browser; close it when done so no window or instance lingers.

When the user wants to look at the app themselves rather than have it verified (「起動して見たい」),
this workflow is the wrong tool — start the dev server from `.claude/rules/gradle.md` —
Development And Packaging Commands and hand over its URL instead; when the ask is ambiguous
between the two, confirm which one is wanted.

## Workflow

1. **Build and serve** the distribution statically
2. **Navigate and wait** for the Splash → Profile transition
3. **Interact** through testTag ids in the a11y mirror
4. **Verify visually** with screenshots
5. **Clean up** — close the browser, stop the server, remove stray output

## Build and serve

Run the production client build from `.claude/rules/gradle.md` — Development And Packaging
Commands, then serve its output:

```bash
python3 -m http.server <port> --directory app/webApp/build/dist/wasmJs/productionExecutable
```

- Navigate with a `localhost` URL. Before choosing the port, inspect
  `server/src/main/kotlin/io/github/kei_1111/server/plugins/Cors.kt`, the canonical allowlist. Use
  an allowed local origin only when the check needs live API data; otherwise any free port is
  valid and loading/error states are expected. The client API base URL is fixed to the deployed
  server, so starting `:server` locally does not restore live data for this workflow.
- Before serving, check the chosen port is free: `lsof -nP -iTCP:<port> -sTCP:LISTEN`. A parallel
  session may already be serving a stale build there. When elements you just added aren't
  found, run the stale-build check from `.claude/rules/ui-testing.md` — Running before suspecting
  the code. If the port is taken, move to a free one unless the check requires an allowlisted
  origin; in that case free the required port without changing the server process.
- Do **not** reuse the development server from `.claude/rules/gradle.md`: it serves a snapshot
  of its startup build (a source edit triggers a live reload back to Splash, but the served
  build is still the old snapshot — even with `--continuous`), and
  automation tooling that writes logs under the repo root makes
  webpack live-reload the page in an endless loop.
- Freeze source edits while verifying — any rebuild or reload sends the app back to Splash and
  invalidates in-memory state. If you must edit, rebuild and restart the verification.

## Navigate and wait

Wait out the Splash → Profile transition before any interaction — input on Splash is ignored
by design. Poll for a stable Profile element from the current `TestTags` source instead of sleeping.
Set the browser locale before asserting visible text; the suite convention is canonical in
`.claude/rules/ui-testing.md` — Writing a Test.

## Interact

Follow `.claude/rules/ui-testing.md` — Interaction Conventions and testTag Placement in full.
- Fall back to coordinate clicks only for untagged elements: take a screenshot, derive the
  coordinates, then a mouse coordinate click. Keyboard input goes through `page.keyboard` as
  usual.
- Some automation bridges do not expose Node globals (`setTimeout`, `Buffer`) inside
  evaluated snippets — use `page.waitForTimeout(...)` and do file comparisons in the shell.

## Verify visually

- A screenshot can lag one frame behind the real state. Before concluding "the change is not
  reflected" or "nothing happened", take a second screenshot of the same state.
- Read the screenshot file and inspect it; resize the viewport to check responsive
  breakpoints (e.g. below the compact breakpoint — canonical: `WindowLayout.kt`).
- Before declaring live behavior verified, distinguish live data from fallback/error states by
  observing the actual API responses (Playwright request/response events), not the pixels alone.
- Save screenshots by absolute path under an untracked directory outside the repository
  (scratchpad or temp directory) — a relative path lands in the repo root and gets swept into
  commits by `git add -A`. Report the saved path for any shot the user may want, and delete
  purely working shots when done.

## Clean up

Close the browser, stop the static server by killing the PID you started — never kill by port
blindly because another session may own that listener — and remove any automation output left
under the repo root.

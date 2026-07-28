# AGENTS.md — server/

Rules for the Ktor server. The root `AGENTS.md` still applies; this file adds the server-specific rules.

- Canonical detail: `.claude/rules/server.md` (routing/service/client layering, `TtlCache` semantics, cancellation, test seam), `.claude/rules/server-testing.md` (test conventions), and `.claude/rules/tdd.md` (TDD process for new server logic).
- Failures fold into `null` at the client layer and into fallbacks at the service layer — keep the `null = failure` contract, and call `currentCoroutineContext().ensureActive()` before swallowing a broad catch.
- Profile source content lives in `ProfileContent.kt` (`DefaultGitHubProfile`); the wasm client keeps a copy in `app/core/data`'s `FallbackProfile` — update both together.
- Validate with `./gradlew :server:test` (CI runs it).

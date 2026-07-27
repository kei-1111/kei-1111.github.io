# AGENTS.md — server/

Rules for the Ktor server. The root `AGENTS.md` still applies; this file adds the server-specific rules.

- Canonical detail: `.claude/rules/server.md` (routing/service/client layering, `TtlCache` semantics, cancellation, test seam) and `.claude/rules/server-testing.md` (test conventions).
- Failures fold into `null` at the client layer and into fallbacks at the service layer — keep the `null = failure` contract, and call `currentCoroutineContext().ensureActive()` before swallowing a broad catch.
- Profile source content lives in `ProfileContent.kt` (`DefaultGitHubProfile`); the wasm client keeps a preview duplicate in `app/feature/profile`'s `ProfilePreviewFixtures.kt` — update both together.
- Validate with `./gradlew :server:test` (CI runs it).

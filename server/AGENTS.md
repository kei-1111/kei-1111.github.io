# AGENTS.md — server/

Rules for the Ktor server. The root `AGENTS.md` still applies; this file adds the server-specific rules.

- Canonical detail: `.claude/rules/server.md`, `.claude/rules/server-testing.md`, and
  `.claude/rules/tdd.md`.
- Failures fold into `null` at the client layer and into fallbacks at the service layer — keep the `null = failure` contract, and call `currentCoroutineContext().ensureActive()` before swallowing a broad catch.
- Profile content placement and synchronization are canonical in
  `.claude/rules/naming-conventions.md` — Text Content.
- Validate per `.claude/rules/working-agreement.md` — Build And Validation.

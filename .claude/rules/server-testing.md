---
paths:
  - "server/src/test/**"
---

# Server Testing

Sibling suites: `ui-testing.md`; `mvi-testing.md` once the planned ViewModel unit tests land.

- Stack: JUnit 5 + kotlin.test assertions; Ktor `testApplication` for route-level tests and
  `MockEngine` to stub the GitHub GraphQL API — tests never hit the real GitHub API.
- Run with `./gradlew :server:test` (CI runs this on every PR).
- Covers routing, the GitHub client, and the TTL cache (single-flight / stale-if-error).
- Follow the existing tests in `server/src/test/` as the reference for structure and naming.

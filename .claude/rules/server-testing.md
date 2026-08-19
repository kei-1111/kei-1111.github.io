---
paths:
  - "server/src/test/**"
---

# Server Testing

Sibling suites: `app-testing.md` (client unit tests; ViewModel specifics in `mvi-testing.md`);
`ui-testing.md` (Playwright). New server logic is written test-first per `tdd.md`.

- Stack: JUnit 6 + kotlin.test assertions; Ktor `testApplication` for route-level tests and
  `MockEngine` to stub the GitHub GraphQL API — tests never hit the real GitHub API.
- Select the command from `.claude/rules/working-agreement.md` — Build And Validation.
- Covers the suites under `server/src/test/` — the directory itself is the canonical list.
- Follow the existing tests in `server/src/test/` as the reference for structure and naming.
- A test class lives in the same package as its subject; a fake shared across test classes is an
  `internal Fake*` class in its own file beside them (`FakePublishedContentClient.kt`).

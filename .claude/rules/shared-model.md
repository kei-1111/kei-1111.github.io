---
paths:
  - "shared/model/**/*.kt"
---

# Shared Wire Contract

The client (GitHub Pages) and server (Cloud Run) deploy independently. For every
`@Serializable` model in this module:

- Add a field only with a default value. A newer client can then decode an older server response;
  the client parser's `ignoreUnknownKeys` handles the opposite deployment order.
- Treat a serialized-name change, field or enum deletion, and field-type change as wire-breaking;
  do not make one without an explicit migration plan. A Kotlin-only rename is safe while its
  `@SerialName` stays fixed.
- An enum addition is supported only through the tolerant list serializers used by
  `GitHubProfile`; keep their unknown-value behavior covered when changing those fields. Treat an
  enum addition outside those serializers as wire-breaking for older clients.

`:server`'s `SharedModelContractTest` pins serializer field names and the raw JSON emitted by the
production Ktor routes, including non-default and nullable-default fields. Minimum validation for a
model or serializer change is canonical in `.claude/rules/working-agreement.md` — Build And
Validation. Shared commonTest conventions live in `.claude/rules/app-testing.md`.

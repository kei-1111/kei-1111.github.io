# AGENTS.md — app/

Rules for the wasm client (`app/*`). The root `AGENTS.md` still applies. Detailed
conventions live in the canonical rules below; keep this file limited to app-scoped
invariants that are useful at the entry point.

## Canonical Rules

- Architecture and MVI: `.claude/rules/mvi-architecture.md`, `.claude/rules/usecase.md`
- Data, DI, and failure boundaries: `.claude/rules/data-layer.md`, `.claude/rules/error-handling.md`
- Navigation: `.claude/rules/navigation.md`
- UI and Preview: `.claude/rules/ui-implementation.md`, `.claude/rules/preview.md`
- Naming and content placement: `.claude/rules/naming-conventions.md`
- Testing: `.claude/rules/app-testing.md`, `.claude/rules/tdd.md`, `.claude/rules/mvi-testing.md`

## App-Scoped Invariants

- Feature modules use UseCases rather than reaching into Repository or data-layer implementations.
- A destination does not reference a sibling destination or its components; the isolation check enforces this.
- The editor and Preview represent the same profile data and must be updated together.
- User-visible wasm changes require the browser smoke-test procedure from `ui-implementation.md`.
- Tests use hand-written fakes and assert observable behavior; do not add a mocking framework.

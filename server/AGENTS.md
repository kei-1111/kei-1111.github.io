# AGENTS.md — server/

Rules for the Ktor server (`server/`). The root `AGENTS.md` still applies. Detailed
conventions live in the canonical rules below; keep this file limited to server-scoped
invariants that are useful at the entry point.

## Canonical Rules

- Implementation, layering, and failure policy: `.claude/rules/server.md`
- Testing: `.claude/rules/server-testing.md`, `.claude/rules/tdd.md`
- Profile content placement: `.claude/rules/naming-conventions.md` — Text Content

## Server-Scoped Invariants

- What a failed upstream fetch means is decided per endpoint in the service layer; do not
  flatten those decisions into one shared fallback.
- Broad catches around suspend I/O must stay cancellation-safe; the mechanism is canonical in
  `.claude/rules/server.md`.

---
paths:
  - ".github/**"
---

# CI/CD

Canonical for what the CI/CD workflows run: the files in `.github/workflows/` themselves —
this rule keeps only the intent and invariants the YAML cannot state. Always-loaded summary and the
pre-push detekt hook: `.claude/rules/git-workflow.md` — CI/CD.

- One independent workflow file per check, each triggered on every PR to `main`. The
  script-check workflows are never gated; every heavy job is docs-only gated.
- Docs-only gate: gated CI and CD files call the reusable `detect-docs-only.yml` (canonical for
  the documentation path patterns). Any unresolvable case (API failure, empty file list) fails
  open and runs normally — the gate job itself failing also falls open, since gated jobs run
  under `!cancelled() && outputs.code != 'false'` (without a status-check function an implicit
  `success()` would skip them). A skipped-by-`if:` job still satisfies required status checks,
  so docs-only PRs stay mergeable.
- PR CI runs the E2E suite against the fast development build; the production binary is
  E2E-gated inside `deploy-app.yml` right before deploying. Merging a PR deploys immediately
  (docs-only changes skip the deploys).
- `warm-playwright-cache.yml` exists so new PR branches hit the Playwright cache on their first
  run: it restores `lookup-only` and is deliberately not docs-only gated — a cache hit already
  skips the rest of the job. Its save key must stay identical to `ui-test.yml`'s restore key.

---
name: project-audit
description: Audit the whole project — survey the live app and repository, run parallel read-only lens reviews (client / server+shared / build+CI+docs / product), and synthesize findings into actionable buckets. Use when the user asks for a whole-project review or audit (プロジェクト全体を監査して, 全体的にレビューして, 改善点を洗い出して), as opposed to reviewing a specific diff (code-reviewer / rules-reviewer) or a PR.
---

# Project audit

## Task overview

Whole-project review: survey the running product and the repository, fan out independent
read-only lens reviews in parallel, and synthesize everything into decision-ready buckets.
The diff-scoped review agents do not cover this; this skill owns the project-wide pass.
It never edits code and never files issues.

## Workflow

1. **Scope** — default to the whole project (live behavior + repository); narrow only when the
   user names an area. Record the audited commit (and whether the worktree was dirty) for
   later re-runs.
2. **First-pass survey (main loop)** — drive the built app in a headless browser (build, serve,
   and interact as `verify-app` does) and walk the repository structure. Collect hotspots and
   first impressions to direct the lenses; do not conclude yet.
3. **Lens reviews** — spawn one read-only subagent per lens (in parallel up to the product's
   agent capacity, staggering the rest), each returning findings with file citations and a
   severity guess:
   - **client** (`app/`): architecture drift, UI consistency, dead or duplicated code
   - **server + shared** (`server/`, `shared/`): API contracts, error handling, caching, limits
   - **build + CI + docs** (`build-logic/`, `.github/`, `docs/`, `ai-docs/`): wiring drift,
     stale or contradictory documentation
   - **product** (from the survey): UX gaps, missing states, improvement ideas
   Keep the lenses independent — no shared conclusions between agents.
4. **Synthesize** — merge and dedupe the findings; verify surprising or high-impact claims
   against the actual code before accepting them; bucket the result into
   **product improvements / code improvements / deletable / leave-as-is (with the reason)**.
5. **Report and stop** — present the buckets with citations and a recommended priority.
   Do not start fixes and do not create issues — issue filing is user-directed
   (Working Agreement — Scope Of A Request); when asked, batch findings into checklist
   issues by kind rather than one issue per finding.

## Notes

- Re-runs are expected as the codebase moves; when the user names a previous audit, diff
  against its recorded commit and focus on what changed.
- Findings must cite what exists at the audited commit — no speculation about unbuilt features.

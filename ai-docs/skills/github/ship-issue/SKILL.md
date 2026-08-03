---
name: ship-issue
description: Take a GitHub Issue all the way to an opened pull request — implement, update docs, commit, and create the PR in one flow. Use when the user asks to handle an Issue through to a PR (「PRまで対応して」, 「最後まで対応して」, ship this Issue). For a working-tree change only, use implement-issue.
---

# Ship issue

## Task overview

Thin orchestrator chaining `implement-issue` → `update-docs` → `create-commit` → `create-pr` and
folding each inner skill's report into one final report. This skill owns only the ordering and the
confirmation points between steps — it never reimplements an inner skill's logic. When an inner
skill needs a decision (plan approval, commit approval, PR body confirmation), surface that
question directly and pause the chain there.

## Workflow

1. **Implement** — run `implement-issue` with the given arguments (Issue number/URL, size
   override, `no-review`); its branch precondition, plan approval, validation, and size-scaled
   review all apply as written
2. **Update docs** — run `update-docs` over the resulting change
3. **Commit** — present the final diff, get the user's confirmation, then run `create-commit`
   (one commit per logical unit)
4. **Create PR** — run `create-pr`; any deviation from the Issue goes into the PR body's
   Summary — reviewers need it there, not in the report
5. **Report** — one consolidated report opening with a prose overview of what was changed and
   why, then changed files, validation results, cross-review rounds with fixed/rejected findings,
   docs updated, commits created, and the PR URL. Always also render the report as an HTML page
   from `references/report-template.html` (shared with `implement-issue`; its fixed sections
   carry only what opening the PR does not give — overview + detail, data-flow diagram,
   Before/After screenshots, follow-ups & known limitations, PR URL) — fill the slots only,
   delete optional sections that do not apply, and never otherwise change structure or CSS —
   and share it (Claude Code: publish it as an Artifact; a product without artifact publishing
   writes the HTML file and reports its path). Then attach the report to the PR created in
   step 4 as a one-line `gh pr comment` with exactly this text: `Execution report for this
   batch (session artifact, private by default — share from the page menu if needed): <report
   URL>` — only when no published URL exists, carry the report's overview instead — so the
   execution context lives with the PR, not only in the session

If an inner step fails or the user stops the chain, report what completed and what remains.

## Argument handling

| Argument | Behavior |
|---|---|
| Issue number / URL | Passed through to `implement-issue` |
| `small` / `medium` / `large` / `no-review` | Passed through to `implement-issue` (size override / skip its review step) |
| (none) | `implement-issue` derives `#<N>` from the current branch name `<type>/#<N>` |

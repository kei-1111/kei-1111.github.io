---
name: triage-pr-reviews
description: Triage PR review comments (inline reviews, review summaries, issue comments, bot reviews) into a concrete fix plan, classifying each as "fix" / "won't fix" / "split into separate issue". Use when the user asks to triage PR review feedback, decide which comments to address, or generate a fix plan for review comments. Code modification is out of scope.
---

# PR Review Triage

## Overview

Fetch every review comment on a PR, classify each into "fix" / "won't fix" / "split into separate issue" with a one- to two-line rationale, and produce a concrete fix plan for the ones marked "fix".

## Workflow

### 1. Identify the target PR

If the user passes a PR number / PR URL as argument, use it. Otherwise, resolve the PR attached to the current branch.

```bash
# With argument
gh pr view <number> --json number,title,url,state,headRefName,baseRefName

# Without argument (current branch)
gh pr view --json number,title,url,state,headRefName,baseRefName
```

If no PR is found, ask the user which PR to target.

### 2. Fetch review comments

Comments live in three separate REST endpoints — fetch all of them:

| Source | Command | Content |
|--------|---------|---------|
| **Inline review comments** | `gh api repos/{owner}/{repo}/pulls/{number}/comments --paginate` | Comments anchored to a diff line (`path` / `line` / `diff_hunk` available) |
| **Review summary** | `gh api repos/{owner}/{repo}/pulls/{number}/reviews --paginate` | Top-level review `body` (Approve / RequestChanges / Comment) |
| **Issue comments** | `gh api repos/{owner}/{repo}/issues/{number}/comments --paginate` | General PR conversation comments |

Resolve `owner` / `repo` with:

```bash
gh repo view --json owner,name -q '.owner.login + "/" + .name'
```

If thread resolution state (`isResolved`) is needed, use GraphQL:

```bash
gh api graphql -F owner=<owner> -F repo=<repo> -F num=<number> -f query='
  query($owner:String!,$repo:String!,$num:Int!){
    repository(owner:$owner,name:$repo){
      pullRequest(number:$num){
        reviewThreads(first:100){
          nodes{
            isResolved
            comments(first:50){
              nodes{ id author{login} body path line }
            }
          }
        }
      }
    }
  }'
```

REST endpoints (`gh api repos/...`) accept `--paginate`. The GraphQL `reviewThreads(first:100)` is enough in practice; if it isn't, page manually with `pageInfo { hasNextPage endCursor }`.

### 3. Organize the comments

Track each comment by:

- Comment ID
- Author (human vs. bot — treat accounts ending in `[bot]`, e.g. `gemini-code-assist[bot]`, as bots)
- Target file / line (inline review comments only)
- Body (summarized)
- Resolved flag (when fetched via GraphQL)
- Thread linkage (`in_reply_to_id` groups replies into a single thread)

Group comments that hit the same file or repeat the same point.

### 4. Verify each comment, then classify

Comments — especially from LLM-based bot reviewers (Copilot, claude[bot], gemini-code-assist[bot], etc.) — frequently surface plausible-sounding but incorrect concerns. Treat every claim as a hypothesis until verified.

**Verify before classifying as "Fix"**:

- Read the cited code, docs, or API. Line numbers and quoted snippets can be stale or misread.
- Cross-check claims about external behavior (GitHub API, library semantics, language features) against current upstream documentation.
- Check whether the suggestion conflicts with `AGENTS.md` or established patterns in this repo.
- Distinguish "the document/code is actually wrong" from "the wording could be slightly more precise" — the latter is a stylistic preference, not a defect.
- For bot reviewers, treat low-effort or generic suggestions as especially likely to be off-base.

A claim that does not survive verification → **Won't fix**, with the verification result as the rationale.

Then sort each comment (or comment group) into one of the three buckets, with a one- to two-line rationale that includes what was verified:

| Bucket | Typical criteria |
|--------|------------------|
| **Fix** | The cited problem actually exists after verification: real bug, real code-quality issue, real project-rule violation, typo |
| **Won't fix** | Verification did not confirm the problem (cited code is fine, API behaves differently than claimed, wording is descriptively correct) / out-of-scope improvement / existing pattern takes precedence / alternative already chosen |
| **Split into separate issue** | Verified concern, but scope is too large / different concern from this PR / better handled in a follow-up PR |

### 5. Build the fix plan

For every "Fix" comment (or group), write:

```markdown
### Fix Plan #<n> — <short title>

- **Target comments**: #<id> by <author> — `<path>:<line>` (when applicable)
- **Issue**: <summary>
- **Approach**: <what to change, in which file, how>
- **Impact**: <other files affected, manual verification steps>
- **Commit shape**: <single commit / split into N commits, with proposed commit message(s)>
```

When several comments collapse into one fix, list all their IDs under `Target comments`.

### 6. Present and get approval

Deliver everything in a single response and wait for the user's call:

1. **Summary** — total comments fetched, breakdown (Fix N / Won't fix M / Split L)
2. **Won't-fix list** — comment IDs with the rejection rationale
3. **Split-out candidates** — proposed English issue titles (`[<Type>]: <title>`) and short descriptions
4. **Fix plans** — numbered using the format above
5. **Next-step decisions**:
   - Proceed with the fix plan?
   - Open the split-out issues now?
   - Reply to any "Won't fix" comments?

## Notes

- **This skill never modifies code.** Stop after the fix plan and wait for user approval.
- **Bot reviews count.** Mechanical or out-of-scope bot suggestions tend to land in "Won't fix", but evaluate on content — don't auto-reject them.
- **Resolved threads** are included by default so the user can re-confirm them. Drop them only when the user says so in plain language (e.g., "skip resolved").
- **Many-comment PRs** (>10): group by file or by repeated issue so the user can scan the picture quickly.
- **Language**: write the classification report and fix plans in Japanese (this is the user-facing output and matches the rest of the project's review workflow).
- **Project-rule alignment**: when shaping a fix, check it against `AGENTS.md` and the current source. If a review comment conflicts with a project rule, surface that conflict explicitly.

## Arguments

| Argument | Example | Behavior |
|----------|---------|----------|
| PR number | `432` | Target that PR |
| PR URL | `https://github.com/kei-1111/kei-1111.github.io/pull/432` | Extract the number from the URL |
| (none) | — | Use the PR attached to the current branch |
| Free-form instruction | "skip resolved", "bots only", "only file X" | Apply as a natural-language filter on fetch / classification |

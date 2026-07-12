---
name: review-by-claude
description: Have Claude Code review code changes (working tree, branch diff, or a PR) as an independent second reviewer. Use when the user asks for a Claude review, a cross-review, or "Claudeにレビューしてもらって".
---

# Review by Claude

## Task overview

Run the Claude Code CLI headlessly (`claude -p`) to get an independent code review of the current changes, verify every finding against the actual code, and report the verified result to the user. This skill never edits code — Claude's review is advisory input.

## Workflow

### 1. Determine the review target

- Argument is a PR number/URL → that PR (`gh pr view <n>`, `gh pr diff <n>`)
- No argument → uncommitted changes (`git status` / `git diff`) if any, otherwise the current branch vs `main` (`git diff main...HEAD`)

State the chosen target explicitly in the final report.

### 2. Compose the review prompt

`claude -p` starts a fresh non-interactive session in the current directory: it automatically loads the repo's `CLAUDE.md` and the matching `.claude/rules/*.md`, can read files and run read-only commands itself, prints the final answer, and exits. The prompt must still be self-contained:

- Name the review target as concrete commands Claude runs itself (e.g. `git diff main...HEAD`, `gh pr diff <n>`) — do not paste large diffs into the prompt
- Required output format per finding: severity / `file:line` / problem / why it matters / suggested fix. Explicitly allow "no findings" — do not force issues into existence
- Instruct Claude to review only — no file edits, no commits

### 3. Run the claude CLI

```bash
claude -p "<prompt>"
```

- Use a generous finite timeout (suggested: 600000 ms / 10 min). If it times out or fails, report that rather than retrying blindly.

### 4. Verify before relaying

Claude is an LLM reviewer — every finding is a hypothesis until verified:

- Read the cited code; line numbers and quoted snippets can be stale or misread
- Check whether a suggestion conflicts with `AGENTS.md` / `.claude/rules/*.md` or an established pattern (e.g. the sanctioned `FallbackContributions` fallback, inline `onIntent`, no error UI)
- A claim that does not survive verification is reported as rejected, with the verification result as the reason

### 5. Present to the user (in Japanese)

- **検証済みの指摘**: severity 順、`file:line` 付き
- **棄却した指摘**: 棄却理由(検証結果)付き
- **自身の見解**: Claude が見落とした点や同意/不同意
- **推奨アクション**: 修正する / しない / 別 Issue 化

Wait for the user's decision. Do not modify code based on the review without explicit approval.

## Notes

- If the `claude` command is unavailable, report that instead of substituting a self-review

## Argument handling

| Argument | Behavior |
|----------|----------|
| PR number / URL | Review that PR |
| Free-form focus (e.g. "パフォーマンス観点で") | Add as a review focus in the prompt |
| (none) | Review uncommitted changes, else current branch vs main |

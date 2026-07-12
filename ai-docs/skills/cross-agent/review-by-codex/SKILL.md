---
name: review-by-codex
description: Have the OpenAI Codex CLI review code changes (working tree, branch diff, or a PR) as an independent second reviewer. Use when the user asks for a Codex review, a cross-review, or "Codexにレビューしてもらって".
allowed-tools: Bash(codex:*), Bash(git:*), Bash(gh:*), Read(*)
---

# Review by Codex

## Task overview

Run `codex exec` in non-interactive mode to get an independent code review of the current changes, verify every finding against the actual code, and report the verified result to the user. This skill never edits code — Codex's review is advisory input.

## Workflow

### 1. Determine the review target

- Argument is a PR number/URL → that PR (`gh pr view <n>`, `gh pr diff <n>`)
- No argument → uncommitted changes (`git status` / `git diff`) if any, otherwise the current branch vs `main` (`git diff main...HEAD`)

State the chosen target explicitly in the final report.

### 2. Compose the review prompt

`codex exec` opens a fresh session that does not see this conversation — the prompt must be self-contained:

- Name the review target as concrete commands Codex runs itself (e.g. `git diff main...HEAD`, `gh pr diff <n>`) — do not paste large diffs into the prompt
- Instruct Codex to review against the project conventions: `AGENTS.md` plus the `.claude/rules/*.md` files relevant to the touched areas (name them)
- Required output format per finding: severity / `file:line` / problem / why it matters / suggested fix. Explicitly allow "no findings" — do not force issues into existence
- Tell Codex to read the cited code and verify each claim before reporting it

### 3. Run codex exec

```bash
codex exec "<prompt>"
```

- Use a generous finite timeout (suggested: 600000 ms / 10 min). If it times out or fails, report that rather than retrying blindly.

### 4. Verify before relaying

Codex is an LLM reviewer — every finding is a hypothesis until verified (same discipline as `triage-pr-reviews`):

- Read the cited code; line numbers and quoted snippets can be stale or misread
- Check whether a suggestion conflicts with `.claude/rules/*.md` or an established pattern (e.g. the sanctioned `FallbackContributions` fallback, inline `onIntent`, no error UI)
- A claim that does not survive verification is reported as rejected, with the verification result as the reason

### 5. Present to the user (in Japanese)

- **検証済みの指摘**: severity 順、`file:line` 付き
- **棄却した指摘**: 棄却理由(検証結果)付き
- **自身の見解**: Codex が見落とした点や同意/不同意
- **推奨アクション**: 修正する / しない / 別 Issue 化

Wait for the user's decision. Do not modify code based on the review without explicit approval.

## Notes

- **No secret leakage**: the prompt is sent to Codex's backend — never include credentials or tokens
- **Read-only**: this skill never edits files
- Each `codex exec` call is a fresh session; nothing carries over between runs

## Argument handling

| Argument | Behavior |
|----------|----------|
| PR number / URL | Review that PR |
| Free-form focus (e.g. "パフォーマンス観点で") | Add as a review focus in the prompt |
| (none) | Review uncommitted changes, else current branch vs main |

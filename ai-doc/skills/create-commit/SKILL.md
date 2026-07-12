---
name: create-commit
description: Generate a commit message that follows the project's Conventional Commits format from the staged changes and run `git commit`. Use when the user asks for a commit, or when work has reached a logical checkpoint that should be committed.
allowed-tools: Bash(git diff:*), Bash(git status:*), Bash(git add:*), Bash(git commit:*), Bash(git log:*), Read
---

# Create commit

Inspect the staged changes, generate a commit message that follows the project convention, and run the commit.

## Convention

Follow the format defined in `.claude/rules/git-workflow.md` (read it first):

- Format: `<type>: <説明>` or `<type>(scope): <説明>`
- Type: feat, fix, docs, refactor, perf, build, ci, chore (English, lowercase)
- Scope: module-ish segment — profile, splash, core, designsystem, app, utils, deps
- **Language: the description is written in JAPANESE** (only type/scope are English). End it in noun form (体言止め — 追加 / 修正 / 削除 / 解消 / 統一 など), matching the existing log.

Real examples from this repository's log:

```
fix(profile): note リンクのアイコンを現行の公式ロゴに修正
feat(profile): ProjectTree を横スクロール可能にしファイル名の見切れを解消
```

## Workflow

1. Run `git status` to review the changes
2. Run `git diff --staged` to inspect the staged contents
3. Run `git log --oneline -5` to see the recent commit style
4. Generate a message that follows the convention
5. After confirming with the user, run `git commit`

---
name: create-commit
description: Create a git commit from the staged changes, with a message following the project's Conventional Commits format. Use only when the user explicitly asks for a commit — never commit unprompted.
---

# Create commit

Inspect the staged changes, generate a commit message that follows the project convention, and run the commit.

## Convention

Follow the Git and PR rules in `AGENTS.md` (read them first):

- Format: `<type>: <description>` or `<type>(scope): <description>`
- Type: feat, fix, docs, refactor, perf, test, build, ci, chore (English, lowercase)
- Scope: module-ish segment — profile, splash, core, designsystem, app, utils, deps
- **Language**: write the description in concise imperative English.

Real examples from this repository's log:

```
fix(profile): use the official note logo
feat(profile): allow horizontal scrolling in ProjectTree
```

## Workflow

1. Run `git status` to review the changes
2. Run `git diff --staged` to inspect the staged contents
3. Run `git log --oneline -5` to see the recent commit style
4. Generate a message that follows the convention
5. After confirming with the user, run `git commit`

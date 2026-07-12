---
name: create-pr
description: Analyze the committed changes on the current branch and create a pull request via the GitHub CLI (gh). Use when the user asks for a PR, or when the branch's work is complete and ready for review.
---

# Create pull request

## Task overview

Inspect the changes on the current branch and create a pull request using the GitHub CLI (gh).

## Convention

Follow the Git and PR rules in `AGENTS.md` (read them first):

- PR title: `[<Type>]: <タイトル>` (the same title as the corresponding Issue, verbatim)
- Branch name: derive the Issue number from `<type>/#<issue番号>` (types: feature, fix, refactor, other)
- Base branch: `main`

## Workflow

1. **Check the current state**
   - Run `git status` to review changes
   - Run `git log` to review recent commits
   - Confirm the current branch and base branch

2. **Analyze the changes**
   - Review every commit message
   - Inspect the modified files
   - Understand the purpose and scope of impact

3. **Compose the PR title and body**
   - Extract the Issue number from the branch name (format: `<type>/#<issue番号>`)
   - Run `gh issue view` to fetch Issue information and reuse the Issue title as the PR title
   - Build the body based on the project PR template (`.github/PULL_REQUEST_TEMPLATE.md`):
     - `## 概要` and `## 実施Issue` are always present
     - Add `## 原因と対処` only for bug fixes
     - Add `## UI変更` (Before/After image table) only when the UI changed

4. **Create the pull request**
   - Run `git push -u origin <branch-name>` if needed
   - Create the PR with `gh pr create`
   - Print the URL of the created PR

## Notes

- If there are uncommitted changes, prompt the user to commit them first
- Always use the corresponding Issue title as the PR title
- Write the title and body in Japanese

## Argument handling

When an argument is provided:

- Use the argument as supplemental context for the PR body

When no argument is provided:

- Generate the body by analyzing the commit messages

Note: the PR title is always the Issue title.

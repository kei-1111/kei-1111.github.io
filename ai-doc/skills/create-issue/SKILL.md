---
name: create-issue
description: Create a GitHub Issue via the GitHub CLI (gh) following this project's issue forms ([Bug] / [Feature] / [Refactor] / [Documentation] / [Research] / [Other]) with an English body. Use when the user asks to file an issue, 起票する, or turn a bug, idea, or task into a GitHub Issue.
allowed-tools: Bash(gh:*), Bash(git:*), Read(*)
---

# Create issue

## Task overview

Compose a GitHub Issue that matches one of the repository's issue forms and create it with `gh issue create`. Issue forms only apply to the web UI, so the CLI must reproduce the same structure by hand: mirror the form's section headings in the body and apply the form's labels explicitly.

## Convention

Follow `.claude/rules/git-workflow.md` (read it first):

- **Title**: `[<Type>]: <タイトル>` — the title itself is written in **Japanese**, matching every existing issue (check `gh issue list --limit 10` for style)
- **Body**: written in **English**, following the section structure of the matching issue form
- **Type**: `Bug` / `Feature` (dedicated forms) or `Refactor` / `Documentation` / `Research` / `Other` (via the Other form)

## Issue forms (`.github/ISSUE_TEMPLATE/`)

Read the matching form before composing, and reproduce its sections as `###` headings **verbatim (headings stay Japanese)** with English content beneath:

| Form | Title prefix | Labels | Body sections (in order) |
|------|--------------|--------|--------------------------|
| `bug_report.yml` | `[Bug]: ` | `Type: Bug`, `Status: Ready` | `### 優先度` / `### 再現手順` / `### 期待する動作` / `### 実際の動作` / `### 補足情報` |
| `feature_request.yml` | `[Feature]: ` | `Type: Enhancement`, `Status: Ready` | `### 優先度` / `### 新機能の説明` / `### 詳細な説明` / `### 補足情報` |
| `other.yml` | `[Refactor]: ` etc. | `Status: Ready` | `### 優先度` / `### 概要` / `### 詳細な説明` / `### 補足情報` |

- `### 優先度` contains exactly one of: `High Priority` / `Medium Priority` / `Low Priority`
- A section with nothing to say gets `_No response_` (matching what the web form renders)

## Workflow

1. **Classify** — decide the issue Type from the user's request; when it is ambiguous (e.g. Refactor vs Other) or the priority is unclear, confirm with the user
2. **Study conventions** — run `gh issue list --limit 10` to match the existing title style; read the matching form in `.github/ISSUE_TEMPLATE/`
3. **Compose** — Japanese title with the correct `[<Type>]: ` prefix; English body mirroring the form's `###` sections in order
4. **Create** — pass the form's labels explicitly:

   ```bash
   gh issue create \
     --title "[Feature]: 作品ページの追加" \
     --label "Type: Enhancement" --label "Status: Ready" \
     --body "$(cat <<'EOF'
   ### 優先度

   Medium Priority

   ### 新機能の説明

   Add a works page that ...

   ### 詳細な説明

   ...

   ### 補足情報

   _No response_
   EOF
   )"
   ```

5. **Report** — print the created issue URL

## Notes

- Do not assign users, milestones, or projects unless the user asks
- One issue = one responsibility; if the request bundles several concerns, propose splitting before creating
- The issue Type determines the branch prefix later (`feature/#N`, `fix/#N`, `refactor/#N`, `other/#N`) — see `.claude/rules/git-workflow.md`

## Argument handling

| Argument | Behavior |
|----------|----------|
| Free-form description | Use as the source material for Type classification and body composition |
| (none) | Ask the user what the issue should cover |

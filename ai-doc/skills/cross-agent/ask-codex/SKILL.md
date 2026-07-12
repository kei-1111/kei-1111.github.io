---
name: ask-codex
description: Ask the OpenAI Codex CLI for a second opinion on the current discussion — implementation approach, written code, or design decision. Use when the user asks for "Codex's opinion", "second opinion", "look at this from another angle", or otherwise wants Codex's perspective on the current topic.
allowed-tools: Bash(codex:*), Read(*)
---

# Ask Codex

## Task overview

Forward the current question or topic to the OpenAI Codex CLI in non-interactive mode, then surface Codex's view to the user along with Claude's own view so the user can make the call. This skill never edits code — Codex's response is treated as advisory input.

## Workflow

### 1. Compose the question for Codex

Build a self-contained prompt for Codex (`codex exec` opens a fresh session that does not see this conversation). Include only what's needed:

- **Background**: what is being designed / built / discussed
- **Concrete subject**: the implementation idea, the file/diff under review, or the open question
- **What kind of input is wanted**: e.g. "are there pitfalls?", "would another approach be cleaner?", "review this diff"
- **Constraints**: relevant project rules (`CLAUDE.md`, `.claude/rules/*`, `AGENTS.md`) when material to the question

If Codex needs to read specific files, name them by path in the prompt (e.g. `feature/profile/.../ProfileScreen.kt`). Codex can read the workspace itself; do not paste large file contents into the prompt.

### 2. Run codex exec

```bash
codex exec "<prompt>"
```

- The prompt is the only argument forwarded; the skill does not auto-attach diffs, file contents, or other context. If the user wants a diff reviewed, they should mention it in their request and Codex will run `git diff` itself.
- Use a generous Bash timeout (suggested: 600000 ms / 10 min). `codex exec` can take from seconds to a few minutes depending on the question.
- Large outputs may be persisted by the Bash tool to a file — read it back if needed.

### 3. Summarize and compare

After Codex returns:

- **Verify before relaying.** Codex is an LLM; its claims may be plausible-sounding but wrong. Cross-check any concrete assertion (API behavior, file contents, line numbers) before presenting it as fact.
- Distill Codex's view into a short summary — key recommendations, risks raised, alternatives suggested.
- Compare it with Claude's own view: agreements, disagreements, points Codex missed.

### 4. Present to the user

Report in this shape:

- **Codex's view**: 3–5 bullets
- **Claude's view**: 3–5 bullets (or "agrees with Codex" if so)
- **Suggested next action**: what to do (apply / discuss further / ignore)

Wait for the user's decision. Do not modify code based on Codex's response without explicit user approval.

## Notes

- **Code changes are out of scope.** Codex's suggestions are advisory; this skill never edits files.
- **No secret leakage.** Before passing a prompt to `codex exec`, confirm it contains no credentials, API keys, or other secrets. The prompt is sent to Codex's backend.
- **Timeout discipline.** Always run `codex exec` with a finite timeout (e.g. 10 min) so the session does not block indefinitely. If it exceeds the timeout, report the failure rather than retrying blindly.
- **No conversation context carries over.** Each `codex exec` call is a fresh session. The prompt must stand on its own — do not assume Codex remembers earlier turns.
- **Language**: present the final report (Codex's view / Claude's view / next action) in Japanese, matching the rest of the project workflow.

## Arguments

| Argument | Example | Behavior |
|----------|---------|----------|
| Free-form question | `Is this MVI design OK?` | Forward verbatim to `codex exec` after composing background context |
| Free-form with file references | `Look at ProfileScreen.kt in feature/profile and suggest improvements` | Codex reads the named files itself |
| (none) | — | Ask the user what they want Codex's opinion on |

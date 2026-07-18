---
name: codex-implement
description: Direct an implementation as Claude (plan, brief, review, validate) while GPT-5.6 Sol executes it through the OpenAI Codex CLI. Use when the user asks to have Codex or Sol implement something, delegate or offload the implementation, "Solに実装させて", "Codexに実装して", "実装を任せて", or wants the director/implementer split between the two models for a change with a clear plan.
---

# Implement by Codex

## Task overview

Split one implementation between the two models: Claude stays the director — scoping, planning, briefing, reviewing, validating — while GPT-5.6 Sol (via the `codex-implementer` subagent) does the code editing. The result is a validated working-tree change; committing and the PR are separate steps (`create-commit` / `create-pr`).

## When to delegate — and when not

Delegate to Sol when the plan is settled enough that the spec determines the code: mechanical edits, well-scoped features with a clear analogous pattern, repetitive multi-file changes. Keep the work on Claude (directly or via the `implementer` subagent) when correctness still needs judgment while typing — architectural choices, UI aesthetics, anything where the plan says "decide as you go". The director must already know what "correct" looks like in order to review the result; if that is not true yet, it is too early to delegate.

## Workflow

1. **Scope and plan** — investigate impact, read the applicable `.claude/rules/*.md` and the nearest analogous code, and settle target files, approach, and validation. For large or ambiguous changes, present the plan and wait for approval first.
2. **Write the brief** — `codex exec` opens a fresh session with no memory of this conversation, so the brief must be self-contained:
   - Goal and target files, by path
   - Approach step by step, naming the analogous pattern to follow (by path)
   - Constraints that apply to the touched area, stated inline (Codex reads `AGENTS.md`, not `.claude/rules/` — name the `AGENTS.md` sections that matter and spell out anything they do not cover)
   - What NOT to touch
3. **Delegate** — write the brief to a file (scratchpad or temp directory) and pass its path, plus the narrowest compile task for the change (e.g. `:app:feature:<name>:compileKotlinWasmJs`), to the `codex-implementer` subagent (contract: `ai-docs/agents/cross-agent/codex-implementer/SKILL.md`). The harness compiles the result on the host and feeds failures back into the same Codex session automatically. Delegate one brief at a time — parallel runs share the working tree and corrupt each other's change attribution.
4. **Review as director** — read the full diff yourself against the plan and the applicable `.claude/rules/*.md`. Sol follows instructions literally: look for constraints the brief failed to state, self-evident comments, and drift from the analogous pattern.
5. **Validate** — the harness already ran the narrow compile when one was passed; run the rest (`./gradlew detekt` — rerun once if autoCorrect reformats).
6. **Iterate or take over** — send review findings back through the subagent as a delta brief with the session id from the delta report (`-s`): the session retains the original brief and context, so state only what is wrong, in which file, and what correct looks like. If the session is no longer resumable, fall back to a self-contained brief (goal, target files, constraints, then the corrections). After two failed rounds on the same problem, stop delegating: fix it directly and note the takeover in the report.
7. **Report** — changed files, validation results, what Sol implemented vs. what the director fixed, and any deviation from the plan with its reason.

## Notes

- **No secret leakage**: the brief is sent to Codex's backend — never include credentials or tokens.
- **No silent fallback**: if the Codex CLI is unavailable, report it and ask whether to continue on Claude — never switch silently.
- **Language**: report to the user in Japanese, matching the rest of the project workflow.

## Argument handling

| Argument | Behavior |
|----------|----------|
| Issue number / URL | Fetch it (`gh issue view`) and use it as the change spec |
| Free-form change description | Use it as the change spec |
| (none) | Ask what should be implemented |

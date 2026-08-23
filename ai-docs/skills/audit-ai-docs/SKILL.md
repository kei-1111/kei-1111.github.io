---
name: audit-ai-docs
description: Run an AI-documentation round — mine every Claude session since the previous round's PR for corrections, repeated manual work, skill-trigger failures, rule gaps, and confirmed traps, then turn the survivors into Candidates entries on the running documentation Issue. Use only on an explicit request for a round (AIドキュメントのラウンド, 前回の更新PR以降のセッションを確認してスキル/規則を改善して) — it fans out analysis agents and is never a side effect of other work.
---

# Audit AI docs

## Task overview

Evidence-first evaluation of the AI documentation (rules, skills, agent definitions, hooks)
against real sessions. The output is a reviewed candidate list on the running documentation
Issue — implementation happens later via `ship-issue` on that Issue. Prefer evidence from real
sessions (a trigger failure, a repeated mistake, a rule nobody reads) over speculative polish;
a rename or restructuring whose only benefit is taste does not survive this bar.

## Workflow

1. **Locate the round** — find the running documentation Issue (search open `[Documentation]`
   Issues for the current round, or ask the user) and the previous round's PR; its merge
   timestamp (`gh pr view <N> --json mergedAt`) is the evidence cutoff.
2. **Extract digests** — run `references/extract_session_digests.py` to pull user-message
   digests from every matching session under `~/.claude/projects` since the cutoff into the
   session scratchpad. Sessions of other projects stay out of scope unless the user says
   otherwise.
3. **Analyze in parallel** — batch the digests by worktree/time and fan out one read-only
   analysis agent per batch, each hunting: user corrections/rollbacks/interruptions, manual
   procedures repeated across sessions (skill candidates), skill-trigger failures and friction,
   the same caution stated twice or more (rule gaps), and confirmed traps. Findings carry a
   session citation and a short quote.
4. **Cross-check** — merge with the observations queue (`~/.claude/observations/queue.jsonl`,
   entries after the cutoff) and drop what already landed: grep the current rules and skills
   for each claim before proposing it, checking the canonical file (for agent definitions,
   `ai-docs/agents/<name>/SKILL.md`, not the `.claude/agents` stub).
5. **Propose** — present the surviving candidates grouped as new skills / new rules /
   brush-ups, each with its evidence count, and note what was verified as already covered.
   Have the proposal independently reviewed when the user asks.
6. **Record and stop** — append the approved candidates to the running Issue's Candidates
   section (re-fetch the body immediately before editing; record rejected-without-change
   decisions under its evaluation notes so they are not re-proposed). No implementation — the
   batch runs when the user ships the Issue.

## Notes

- The extraction script and the analysis fan-out read personal session logs — never commit
  digests, and keep them in the scratchpad only.
- Batch the resulting work per `.claude/rules/git-workflow.md` — Issues.

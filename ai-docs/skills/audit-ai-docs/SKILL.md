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
Rounds form a successor chain: each running documentation Issue identifies its predecessor and
the PR that landed the predecessor's batch; after the current batch PR lands, close the Issue and
open the next round when evaluation should continue. Every running Issue contains a
`### Candidates` checklist for newly approved findings and `### Evaluation notes` for
rejected-without-change decisions.

## Workflow

1. **Locate the round** — find the running documentation Issue (search open `docs:` Issues
   for the current round, or ask the user) and the previous round's PR; its merge
   timestamp (`gh pr view <N> --json mergedAt`) is the evidence cutoff.
2. **Extract digests** — run `references/extract_session_digests.py` with a fresh empty output
   directory in the session scratchpad to pull only post-cutoff user messages from every matching
   session under `~/.claude/projects`. Sessions of other projects stay out of scope unless the
   user says otherwise.
3. **Analyze in parallel** — batch the digests by worktree/time and fan out one read-only
   analysis agent per batch, each hunting: user corrections/rollbacks/interruptions, manual
   procedures repeated across sessions (skill candidates), skill-trigger failures and friction,
   the same caution stated twice or more (rule gaps), and confirmed traps. Findings carry a
   session citation and a short quote.
4. **Cross-check** — merge with entries after the cutoff for the same project(s) in the
   observations queue (`~/.claude/observations/queue.jsonl`) and drop what already landed: grep
   the current rules and skills for each claim before proposing it, checking the canonical file
   (for agent definitions, `ai-docs/agents/<name>/SKILL.md`, not the `.claude/agents` stub).
5. **Propose** — present the surviving candidates grouped as new skills / new rules /
   brush-ups, each with its evidence count, and note what was verified as already covered.
   Have the proposal independently reviewed when the user asks.
6. **Record and stop** — append the approved candidates to the running Issue's Candidates
   section (re-fetch the body immediately before editing; record rejected-without-change
   decisions under its evaluation notes so they are not re-proposed). No implementation — the
   batch runs when the user ships the Issue.

## Notes

- The extraction script and the analysis fan-out read personal session logs — never commit
  digests, keep them in the scratchpad only, and use a fresh output directory for every run so
  evidence outside the cutoff cannot survive from an earlier extraction.
- Batch the resulting work per `.claude/rules/git-workflow.md` — Issues.

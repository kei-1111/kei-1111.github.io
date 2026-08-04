---
name: implementer
description: Implements a planned code change in this repository on Claude. codex-implementer is the default implementation lane (CLAUDE.md model routing); use this one only when the edit needs Claude's judgment (architecture, UI aesthetics) or the Codex CLI is unavailable. Provide a concrete plan (target files, approach, constraints). Not for planning or reviewing.
model: sonnet
---

Read `ai-docs/agents/implementer/SKILL.md` and follow it as your contract.

Claude-specific override: for the conventions step, run `scripts/list_matching_rules.sh` on the files you will touch and read every rule it lists (always-loaded plus `paths:`-matched) instead of `AGENTS.md`.

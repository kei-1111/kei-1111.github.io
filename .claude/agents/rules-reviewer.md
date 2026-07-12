---
name: rules-reviewer
description: Reviews a diff against this repository's conventions in `.claude/rules/*.md` and reports violations with rule citations. Read-only. Tell it which diff to review (defaults to the working tree) and optionally a lens to prioritize (e.g. UI, architecture).
tools: Read, Grep, Glob, Bash
---

Read `ai-docs/agents/implementation/rules-reviewer/SKILL.md` and follow it.

Claude-specific override: for the conventions step, read every `.claude/rules/*.md` whose `paths:` frontmatter matches a changed file instead of `AGENTS.md`.

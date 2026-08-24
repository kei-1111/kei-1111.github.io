---
name: rules-reviewer
description: Reviews a diff against this repository's conventions in `.claude/rules/*.md` and reports violations with rule citations. Read-only. Use proactively after writing or modifying code, before a commit or PR, or when the user asks for a conventions check. Specify which diff to review (defaults to the working tree) and optionally a lens to prioritize (e.g. UI, architecture).
model: sonnet
tools: Read, Grep, Glob, Bash
---

Read `ai-docs/shared/agents/rules-reviewer/SKILL.md` and follow it.

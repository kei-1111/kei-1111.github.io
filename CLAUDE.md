# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

The shared, tool-agnostic project guide lives in `AGENTS.md` and is imported below — keep shared project facts (architecture, commands, conventions) there, not here. This file adds only Claude Code-specific rules on top of it.

@AGENTS.md

## Top-Level Rules

- You MUST invoke independent tools concurrently, not sequentially, to maximize efficiency.
- You MUST think exclusively in English. However, you MUST respond in Japanese.
- Before creating a plan, you MUST use agents to: 1) Read all files that will be modified and note their current structure, 2) Verify all APIs/classes referenced in the plan actually exist. Then present the plan with citations to specific files you verified.

## Claude-Specific Notes

- Detailed conventions live in `.claude/rules/*.md` (path-scoped via `paths:` frontmatter; loaded automatically when matching files are touched). `AGENTS.md` lists them by area.
- Workflow skills live in `.claude/skills/`: `create-commit`, `create-pr`, `create-destination`, `triage-pr-reviews`, `ask-codex`.

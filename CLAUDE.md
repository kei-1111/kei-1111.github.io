# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository. It is intentionally thin: detailed conventions live in `.claude/rules/*.md` (loaded automatically — every session when a rule has no `paths:`, else path-scoped) and the project guide for all coding agents is `AGENTS.md`, which references `.claude/rules/*.md` for canonical detail.

## Project

kei-1111.github.io is a Kotlin / Compose Multiplatform portfolio web application whose UI mimics the Android Studio New UI with switchable Islands Dark and Light themes and a switchable Japanese/English display language.

- **wasmJs** is the only distribution target (GitHub Pages). **Android** has two roles — rendering commonMain `@Preview` and running the client unit tests as host tests — never shipped.
- Module roles are canonical in `docs/ModuleOverview.md`.
- Multimodule Clean Architecture (`app:feature → app:core:domain → app:core:data`) + MVI, Metro DI, Navigation 3.
- `MaterialTheme` is not used — use `KeiTheme(isDark)` and `KeiTheme.colors` / `.icons`; theme state is owned by `app:webApp`'s `App`.

## Top-Level Rules

Claude-specific additions on top of the always-loaded `.claude/rules/working-agreement.md`:

- Run independent read-only investigations concurrently rather than sequentially; use agents when the reading is sizable.
- Present plans with citations to the files you verified.

## Before Editing

Canonical: `.claude/rules/working-agreement.md` — Before Editing / Instruction Priority. In addition:

- Read the applicable `.claude/rules/*.md` for the area being changed.
- Refer to `docs/ArchitectureOverview.md` / `docs/ModuleOverview.md` (and `AGENTS.md`) when needed.

## Working Principles

- Follow `.claude/rules/working-agreement.md` in full.
- Model routing: once an implementation plan is settled, prefer delegating the code editing to the `codex-implementer` subagent (the official Codex CLI; model pinned in `scripts/codex_implement.sh`), keeping planning, diff review, and judgment in the main loop. Judgment-heavy edits (architecture, UI aesthetics) stay on Claude. This rule also picks the lane when a skill step names the `implementer` subagent; review-lane mappings are canonical in `.claude/rules/working-agreement.md` — Before Handing Off. Run only one implementation lane at a time in a working tree.
- Run the narrowest relevant validation (canonical: `.claude/rules/working-agreement.md` — Build And Validation).
- Commit messages and GitHub-authored text are written in English (see `.claude/rules/git-workflow.md`).

## Skills

Skills are auto-discovered from `.claude/skills/` — no list is maintained here. See
`ai-docs/README.md` for the canonical layout and sharing rules.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository. It is intentionally thin: detailed conventions live in `.claude/rules/*.md` (path-scoped, loaded automatically) and the self-contained project guide for all coding agents is `AGENTS.md`.

## Project

kei-1111.github.io is a Kotlin / Compose Multiplatform portfolio web application whose UI mimics the Android Studio IDE (New UI / Islands Dark).

- **wasmJs** is the only distribution target (GitHub Pages). **Android** exists only to render commonMain `@Preview` — never shipped.
- Multimodule Clean Architecture (`feature → core:domain → core:data`) + MVI, Metro DI, Navigation 3.
- `MaterialTheme` is not used — use `KeiTheme`.

## Top-Level Rules

- You MUST invoke independent tools concurrently, not sequentially, to maximize efficiency.
- You MUST think exclusively in English. However, you MUST respond in Japanese.
- Before creating a plan, you MUST use agents to: 1) Read all files that will be modified and note their current structure, 2) Verify all APIs/classes referenced in the plan actually exist. Then present the plan with citations to specific files you verified.

## Before Editing

- Inspect the current implementation and its nearest analogous code.
- Read the applicable `.claude/rules/*.md` for the area being changed.
- Refer to `docs/ArchitectureOverview.md` / `docs/ModuleOverview.md` (and `AGENTS.md`) when needed.
- Treat current source code as authoritative when documentation has drifted.

## Working Principles

- Make the smallest coherent change; preserve unrelated working-tree changes.
- Do NOT write self-evident comments. Comment only non-obvious constraints or rationale (why, not what).
- Run the narrowest relevant validation (`./gradlew :feature:<name>:compileKotlinWasmJs`, `./gradlew detekt` — rerun detekt once if autoCorrect reformats; never fix import ordering manually).
- Commit messages and GitHub-authored text are written in English (see `.claude/rules/git-workflow.md`).

## Skills

Skills are auto-discovered from `.claude/skills/` — no list is maintained here. Shared skills are canonical in `ai-doc/skills/` (symlinked in); Claude-only skills are real directories. See `ai-doc/README.md` for the layout and sharing rules.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository. It is intentionally thin: detailed conventions live in `.claude/rules/*.md` (path-scoped, loaded automatically) and the project guide for all coding agents is `AGENTS.md`, which references `.claude/rules/*.md` for canonical detail.

## Project

kei-1111.github.io is a Kotlin / Compose Multiplatform portfolio web application whose UI mimics the Android Studio New UI with switchable Islands Dark and Light themes.

- **wasmJs** is the only distribution target (GitHub Pages). **Android** exists only to render commonMain `@Preview` — never shipped.
- Three top-level trees: `app/` (wasm client), `server/` (Ktor API on Cloud Run, serves profile/contributions from the GitHub GraphQL API), `shared/model/` (models shared by both).
- Multimodule Clean Architecture (`app:feature → app:core:domain → app:core:data`) + MVI, Metro DI, Navigation 3.
- `MaterialTheme` is not used — use `KeiTheme`, `KeiThemeController`, and `KeiTheme.icons`.

## Top-Level Rules

- Run independent read-only investigations concurrently rather than sequentially.
- Before planning a non-trivial change, read the files to be modified and verify that the APIs/classes the plan references actually exist (use agents when the reading is sizable). Present the plan with citations to the files you verified.

## Before Editing

- Inspect the current implementation and its nearest analogous code.
- Read the applicable `.claude/rules/*.md` for the area being changed.
- Refer to `docs/ArchitectureOverview.md` / `docs/ModuleOverview.md` (and `AGENTS.md`) when needed.
- Treat current source code as authoritative when documentation has drifted.

## Working Principles

- Follow the Working Agreement in `AGENTS.md` (smallest coherent change, comment policy, documentation concision).
- Model routing: once an implementation plan is settled, prefer delegating the code editing to the `codex-implementer` subagent (GPT-5.6 Sol via the official Codex CLI), keeping planning, diff review, and judgment in the main loop. Judgment-heavy edits (architecture, UI aesthetics) stay on Claude. This rule also picks the lane when a skill step names the `implementer` subagent. Run only one implementation lane at a time in a working tree.
- Run the narrowest relevant validation (`./gradlew :app:feature:<name>:compileKotlinWasmJs`, `./gradlew detekt` — rerun detekt once if autoCorrect reformats; never fix import ordering manually).
- Commit messages and GitHub-authored text are written in English (see `.claude/rules/git-workflow.md`).

## Skills

Skills are auto-discovered from `.claude/skills/` — no list is maintained here. All skills are canonical in `ai-docs/skills/<group>/` and symlinked in flat. See `ai-docs/README.md` for the layout and sharing rules.

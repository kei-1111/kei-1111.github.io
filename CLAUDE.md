# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository. It is intentionally thin: detailed conventions live in `.claude/rules/*.md` (loaded automatically — every session when a rule has no `paths:`, else path-scoped) and the project guide for all coding agents is `AGENTS.md`, which references `.claude/rules/*.md` for canonical detail.

## Project

kei-1111.github.io is a Kotlin / Compose Multiplatform portfolio web application whose UI mimics the Android Studio New UI with switchable Islands Dark and Light themes and a switchable Japanese/English display language.

- **wasmJs** is the only distribution target (GitHub Pages). **Android** has two roles — rendering commonMain `@Preview` and running the client unit tests as host tests — never shipped.
- Four top-level trees: `app/` (wasm client), `server/` (Ktor API on Cloud Run, serves profile/contributions/issues from the GitHub GraphQL API), `shared/model/` (models shared by both), `test/` (Playwright E2E — `test/tags/` holds `testTag` constants shared with `app:feature:*`, `test/e2e/` drives a built distribution in a real browser).
- Multimodule Clean Architecture (`app:feature → app:core:domain → app:core:data`) + MVI, Metro DI, Navigation 3.
- `MaterialTheme` is not used — use `KeiTheme(isDark)` and `KeiTheme.colors` / `.icons`; theme state is owned by `app:webApp`'s `App`.

## Top-Level Rules

- Run independent read-only investigations concurrently rather than sequentially.
- Before any non-trivial edit or assertion, read the files involved and verify what you reference — API/class existence, the resolved dependency version, the running build, live-vs-fallback data (canonical: `.claude/rules/working-agreement.md`; use agents when the reading is sizable). Present plans with citations to the files you verified.
- Escalate when stuck: after a few failed attempts without a confirmed root cause, consult the user instead of applying speculative fixes (canonical: `.claude/rules/working-agreement.md`).
- Goal-driven execution: define verifiable success criteria before a non-trivial change and validate against them before reporting completion (canonical: `.claude/rules/working-agreement.md`).

## Before Editing

- Inspect the current implementation and its nearest analogous code.
- Read the applicable `.claude/rules/*.md` for the area being changed.
- Refer to `docs/ArchitectureOverview.md` / `docs/ModuleOverview.md` (and `AGENTS.md`) when needed.
- Treat current source code as authoritative when documentation has drifted.

## Working Principles

- Follow `.claude/rules/working-agreement.md` (smallest coherent change, comment policy, documentation concision — loaded in every session).
- Model routing: once an implementation plan is settled, prefer delegating the code editing to the `codex-implementer` subagent (GPT-5.6 Sol via the official Codex CLI), keeping planning, diff review, and judgment in the main loop. Judgment-heavy edits (architecture, UI aesthetics) stay on Claude. This rule also picks the lane when a skill step names the `implementer` subagent; the independent review lane maps to the `rules-reviewer` and `code-reviewer` agents run independently, and the cross-model reviewer to the `codex-review` skill. Run only one implementation lane at a time in a working tree.
- Run the narrowest relevant validation (`./gradlew :app:feature:<name>:compileKotlinWasmJs`, `./gradlew detekt` — rerun detekt once if autoCorrect reformats; never fix import ordering manually).
- Commit messages and GitHub-authored text are written in English (see `.claude/rules/git-workflow.md`).

## Skills

Skills are auto-discovered from `.claude/skills/` — no list is maintained here. All skills are canonical in `ai-docs/skills/<group>/` and symlinked in flat. See `ai-docs/README.md` for the layout and sharing rules.

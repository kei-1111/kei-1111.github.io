# AGENTS.md

This file gives coding agents project-specific guidance for working in this repository. It applies to the whole repository; nested `AGENTS.md` files in `app/`, `server/`, `shared/`, and `test/` add per-tree rules, and the closest file to the code being changed takes precedence.

## Instruction Priority

Canonical detail: `.claude/rules/working-agreement.md` — Instruction Priority.

## Project Overview

kei-1111.github.io is a portfolio web application for kei-1111 (basic info, works, skills, SNS links), also serving as a Compose Multiplatform (CMP) learning project. Deployed at https://kei-1111.github.io/ via GitHub Pages.

The UI mimics the Android Studio IDE New UI with switchable Islands Dark and Light themes and a switchable Japanese/English display language: a project tree, a code editor showing the profile as Kotlin source with syntax highlighting, and a Compose Preview pane rendering the actual profile card. Editor code and Preview content must stay in sync.

Tech stack:

- Kotlin / Compose Multiplatform
- **wasmJs** — the only distribution target for the client (browser, GitHub Pages)
- **Android** — exists for exactly two roles: rendering commonMain `@Preview` in the IDE (layoutlib) and running the client unit tests on the local JVM (host tests — `.claude/rules/app-testing.md` / `mvi-testing.md`). Never shipped, no Android runtime features
- **Ktor server** (`server/`, JVM) — serves the portfolio API backed by the GitHub GraphQL API,
  deployed to Cloud Run
- Multimodule Clean Architecture + MVI using `MviViewModel<ViewModelState, State, Intent>`
- Metro DI (`@ContributesBinding` / `@SingleIn` / `@Inject`), `metrox-viewmodel` (`metroViewModel()`)
- Navigation 3 (`androidx.navigation3`), a single `NavDisplay` + `NavKey` back stack
- kotlinx.serialization
- detekt (autoCorrect enabled locally, disabled on CI)
- Custom `KeiTheme` design system (Islands Dark/Light colors, typography, shapes, and icons); the active theme is hoisted state owned by `app:webApp`'s `App` and passed as `KeiTheme(isDark)`. `MaterialTheme` is NOT used

## Read First

Background references — when they drift, current source code wins:

- `docs/ArchitectureOverview.md` — data flow, DI, navigation (Japanese)
- `docs/ModuleOverview.md` — module dependency graph and per-module responsibilities (Japanese)
- `ai-docs/README.md` — how AI-tooling assets are laid out and shared between Claude Code and Codex
- `.claude/rules/*.md` — applicable agent guidance; its precedence and drift policy are defined by
  `.claude/rules/working-agreement.md`, not by the background-reference rule above

Workflow skills automate common flows and are auto-discovered by each tool; no skill list is
maintained here. See `ai-docs/README.md` for the canonical layout and sharing rules.

Only reference rule or workflow files that currently exist in this repository. Do not assume untracked workflow skills, tests, or Android runtime infrastructure are available.

## Working Agreement

Apply `.claude/rules/working-agreement.md` in full.

## Module Roles

Module responsibilities and the dependency graph are canonical in `docs/ModuleOverview.md`.
Per-tree rules live in the nested `AGENTS.md` files named at the top of this document.

## Build And Validation

Canonical detail: `.claude/rules/working-agreement.md` — Build And Validation. Never claim browser
behavior was verified from compilation alone.

## Git And PR Rules

Follow `.claude/rules/git-workflow.md` in full. Git and GitHub mutations require an explicit request
from the user; scope rules are canonical in `.claude/rules/working-agreement.md`.

## Dependency Updates

Follow `.claude/rules/gradle.md` — Dependency Updates in full.

## Safety And Maintenance

Follow `.claude/rules/working-agreement.md` — Safety And Maintenance in full.

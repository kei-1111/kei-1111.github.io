<!-- Keep this file and the Japanese version README.md in sync when editing. -->
<p align="right"><sub><a href="README.md">🌐 日本語</a></sub></p>

## What is kei-1111.github.io
This repository (kei-1111.github.io) hosts a web application built to introduce kei-1111.

The UI is designed as an IDE that mimics Android Studio (New UI).

### Features
- IDE-style UI composed of a project tree, editor, preview, and tool windows
- Theme switching (Islands Dark / Islands Light)
- Display language switching (Japanese / English)
- Search Everywhere (fuzzy search across pages, links, and actions)
- A terminal panel that accepts commands
- Android-Studio-style balloon notifications in the bottom-right corner (updates since your last visit, and a GitHub sync warning)

### What it shows
- Self-introduction (a README rendered in the editor)
- Live GitHub profile stats, pinned repositories, and language share
- Contribution calendar
- Open Issue list (TODO panel)
- Merged pull request changelog (Git panel)
- Links to social media
- Third-party licenses

## Goals of this app
This app was built with three main goals in mind.

- **Build a portfolio that reads as an Android developer's work**
  I use Jetpack Compose day to day, so I chose to implement this portfolio with Compose Multiplatform (CMP). I also wanted it to be immediately recognizable as the work of an Android developer, so I gave it an Android Studio-style UI.
- **Push AI-assisted development as far as it can go**
  I researched documentation practices for working with AI extensively, and settled on consolidating AI-facing docs under `ai-docs/` with symlinks into each agent's expected reference location.
- **Take on a first server-side implementation with AI's help**
  Since I write Kotlin day to day, I implemented the backend in server-side Kotlin (Ktor). I also wanted to try a monorepo setup, so client and server live in one repository as a multi-module project, sharing code through a shared module.

## App URL
https://kei-1111.github.io/

## Screenshots
| Desktop | Mobile |
|-------|-------|
| <img src="https://github.com/user-attachments/assets/831d240d-adb7-4082-ae9d-6f3ad0a94d84" width="650" /> | <img src="https://github.com/user-attachments/assets/fba370ab-8263-46c2-9b0a-47e79412314e" width="250" /> |
| <img src="https://github.com/user-attachments/assets/4df213dd-3b96-499b-bb22-d68f75644888" width="650" /> | <img src="https://github.com/user-attachments/assets/6d4febf8-55b4-446f-8824-35872c0d99ea" width="250" /> |

## Architecture
The client (`:app`) is a multi-module project combining Clean Architecture (`app:feature` → `app:core:domain` → `app:core:data`) with the MVI pattern. The only distribution target is wasmJs; the Android target is a development-only target used to render `@Preview`s and run unit tests as host tests.

Data is served by a self-built API server (`:server`, Ktor / Cloud Run). The server fetches profile stats, pinned repositories, language share, contributions, open Issues, and merged pull requests live from the official GitHub GraphQL API and exposes them as `GET /api/profile` / `GET /api/contributions` / `GET /api/issues` / `GET /api/changelog`, keeping the PAT (access token) secret on the server side. Client and server share a JSON contract through the shared DTO module `:shared:model`.

See the following for details.
- [docs/ArchitectureOverview.en.md](docs/ArchitectureOverview.en.md): architecture, data flow, DI, navigation
- [docs/ModuleOverview.en.md](docs/ModuleOverview.en.md): module structure and dependencies

## Tech Stack

| Area | Technology | Notes |
|-------------|-------------|-------------|
| Language | Kotlin | Type-safe, concise syntax |
| UI framework | Jetpack Compose (Compose Multiplatform) | Android's UI framework, also used on the web |
| DI | Metro | Compile-time DI; automatic binding of Repository/UseCase/ViewModel |
| Navigation | Navigation 3 | Type-safe screen transitions via NavKey |
| Backend | Ktor | Self-built API server (`:server`) that serves profile, contribution, open Issue, and merged PR data |
| External API | GitHub GraphQL API | Stats, pinned repositories, language share, contributions, open Issues, and merged PRs fetched live via the server (PAT kept secret on the server) |
| Deployment (frontend) | GitHub Pages | Automated deployment via GitHub Actions |
| Deployment (server) | Cloud Run | Automated deployment to a scale-to-zero container runtime |
| CI/CD | GitHub Actions | Automatic code analysis/tests on Pull Requests, automatic deployment on merge to main |
| Static analysis | detekt | Used to maintain code quality |
| Unit testing | kotlin-test | Server tests for `:server` and client unit tests (run as Android host tests) |
| E2E testing | Playwright | UI regression tests that verify the built app in a real browser |

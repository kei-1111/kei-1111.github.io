# Git Workflow (Project)

Project-specific seams of `.claude/rules/git-workflow.md` (the shared core).

- Commit `test` type applies to: the `:server` test suite (`server/src/test/`), the client unit tests (`app/**/src/commonTest/`), the `shared/model` commonTest suite, the Playwright E2E suite (`:test:e2e`), the Konsist conventions suite (`:test:conventions`), or the custom detekt rule tests (`detekt-rules/src/test/`)
- Observed scopes: `profile`, `splash`, `core`, `designsystem`, `app`, `utils`, `deps`, `server`, `shared`, `e2e` — examples: `fix(profile): allow horizontal scrolling in TerminalPanel`, `feat(shared): add Work model to the client/server JSON contract`
- `research` is a project-specific extra Issue type (template prefix)
- CI/CD: canonical detail in `.claude/rules/ci-cd.md` and the workflow files in `.github/workflows/`. `PreToolUse` hooks (`.claude/hooks/pre-push-*.sh`, wired in `.claude/settings.json`) gate `git push` commands: detekt must pass cleanly, and `ApiConfig.kt` at HEAD must match the production origin pinned by `ApiConfigTest`; each hook source owns its exact detection and command behavior

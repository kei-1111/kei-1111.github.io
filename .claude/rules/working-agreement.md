# Working Agreement

Canonical working rules for every coding agent in this repository. Claude Code loads this rule
in every session; Codex reaches it through `AGENTS.md`, which summarizes and points here. When
they drift, this file wins.

## Instruction Priority

When guidance conflicts, use this order:

1. The user's current request
2. The closest applicable agent guidance (this file and the other `.claude/rules/*.md`; for Codex, the closest `AGENTS.md`)
3. Current source code and build configuration
4. `docs/ArchitectureOverview.md` and `docs/ModuleOverview.md`

Treat source code as authoritative when generated documentation, examples, or copied patterns
have drifted. Preserve this project's established targets, navigation structure, previews,
dispatchers, resources, and validation approach.

## Before Editing

- Inspect the files being changed and their nearest analogous implementation.
- Check `git status`; preserve user changes and avoid unrelated cleanup.
- Verify referenced APIs, tasks, modules, and paths in the current checkout instead of relying on documentation alone.
- For a non-trivial change, define verifiable success criteria first — the narrowest validation that must pass and, for user-visible UI changes, what to confirm in the browser — and validate against them before reporting completion.

## While Editing

- Make the smallest coherent change that satisfies the request.
- Follow existing module boundaries and naming before introducing a new abstraction.
- Keep refactors separate from behavior changes unless the refactor is required.
- Do not edit generated files or build output.
- Keep documentation concise and proportional; prefer one clear instruction over repeated wording, exhaustive safeguards, or speculative edge cases.
- Escalate when stuck: after a few failed attempts without a confirmed root cause, stop and consult the user instead of applying speculative fixes.

## Comments

- Comments are exceptional: the default for agent-written code is no comments at all.
- The only admissible comment states a constraint the code cannot express — a workaround pinned to an external bug, a non-obvious invariant — and must be individually justifiable. Anything else, including correct-but-derivable rationale, is deleted.
- A comment lives in the file it describes; never describe another file or module from elsewhere.
- A stale comment is deleted or trimmed to its non-derivable part, never rewritten into a cross-reference.

## Scope Of A Request

- Filing an issue or asking for an opinion is not a signal to start implementing. After creating or amending an issue, stop with a completion report — no branch creation, no implementation reconnaissance — until the user explicitly asks. Consultative phrasing requests an opinion, not execution.
- Do not commit, push, create an Issue, or open a PR unless the user asks for that action.

## Build And Validation

Prefer the narrowest command that covers the change. Suggested validation by change type:

| Change | Minimum validation |
|---|---|
| Kotlin in one feature | `./gradlew :app:feature:<name>:compileKotlinWasmJs` |
| Unit-tested logic (`app:core:api` Api clients, `app:core:common` helpers, `app:core:data` Repositories + `SingleFlightCache`, `app:core:local` DataSources, `app:core:domain` UseCases, `app:core:mvi`, feature ViewModels) | `./gradlew :<module>:testAndroidHostTest` (client unit tests; CI runs them) |
| `shared:model` models or serializers | `./gradlew :shared:model:jvmTest :shared:model:wasmJsTest` (commonTest on both consuming targets; CI runs them) |
| Compose UI or Preview | Feature wasm compile + `./gradlew :app:feature:<name>:compileAndroidMain` |
| Core module or cross-module API | Compile every directly affected consumer |
| Navigation, DI, Gradle, or app wiring | `./gradlew :app:webApp:wasmJsBrowserDistribution` |
| Server Kotlin | `./gradlew :server:test` (compiles and runs the server test suite) |
| Formatting or lint-sensitive Kotlin | `./gradlew detekt`; rerun if auto-correct changed files |
| User-visible wasm UI | Production build and, when practical, the browser smoke test (`.claude/rules/ui-implementation.md`) |
| E2E test infra (`test/tags`, `test/e2e`) | `./gradlew :test:e2e:compileTestKotlin`; to actually run it, serve `:app:webApp:wasmJsBrowserDistribution`'s output and `./gradlew :test:e2e:test -PbaseUrl=...` |

- The `:app:webApp:` prefix on the dev-server task is required — an unqualified `wasmJsBrowserDevelopmentRun` can start a different module's dev server on the same port.
- detekt autoCorrect quirks (a reformat can fail the first run — rerun it; never fix import ordering manually) and key rules: `.claude/rules/gradle.md` — detekt (canonical home).
- Test suites: `:server:test` per `.claude/rules/server-testing.md`; the `shared:model` commonTest (`:shared:model:jvmTest` / `:shared:model:wasmJsTest`; CI: `shared-test.yml`); the client unit tests (`testAndroidHostTest`) per `.claude/rules/app-testing.md` with ViewModel specifics in `.claude/rules/mvi-testing.md`; `:test:e2e` per `.claude/rules/ui-testing.md`. New logic on both the client and `:server` follows TDD per `.claude/rules/tdd.md`.
- Do not claim browser behavior was verified when only compilation or static analysis was run; the browser smoke test procedure is `.claude/rules/ui-implementation.md` — Browser Smoke Test (canonical home).
- Full command list (dev server, production build, server run, E2E): `.claude/rules/gradle.md` — Build Commands (canonical home).

## Before Handing Off

- Review the final diff for accidental or unrelated changes.
- Verify before asserting: check API existence and behavior against the resolved dependency version or official sources; confirm the running build actually contains the change before diagnosing from runtime observations; distinguish live data from fallbacks before declaring end-to-end success; separate observation from speculation when reporting.
- When a skill step names the independent review lane, it maps to the `rules_reviewer` and `code_reviewer` agents run independently; a cross-model reviewer exists only on Claude Code (the `codex-review` skill — see `CLAUDE.md`).
- Run the narrowest relevant validation, expanding to broader checks for cross-module or release-impacting changes.
- Report what changed, what was validated, and anything not validated.

## Safety And Maintenance

- Never expose secrets, credentials, tokens, signing material, or machine-specific configuration.
- The Android target has two roles only — Preview rendering and ViewModel host tests: androidMain actuals may be no-op or no-network stubs (`openUrl` doing nothing, `createHttpClient` using a 503 `MockEngine`, etc.) — never add Android-specific runtime features or network calls there.
- Declare all dependencies in `gradle/libs.versions.toml` and reference them via the version catalog, including inside convention plugins (`libs.findLibrary(...)`). Do NOT use the deprecated `compose.dependencies.*` Gradle accessors — specify artifacts directly.
- Prefer the existing convention plugins (`kei_1111.detekt`, `kei_1111.kmp.wasm`, `kei_1111.cmp`, `kei_1111.kmp.feature`, `kei_1111.kmp.shared`, `kei_1111.metro`) over ad hoc Gradle configuration.
- Do not add heavy dependencies without approval.
- Do not rewrite large areas, rename public APIs, or move code across modules unless the task requires it.
- Never discard or overwrite unrelated working-tree changes.
- When generated templates or docs disagree with current source code, the source wins.
- Keep `AGENTS.md` and this file updated together when agent-level instructions change.

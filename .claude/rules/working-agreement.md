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
have drifted. This source-over-document rule does not override applicable agent guidance in
`.claude/rules/*.md`. Preserve this project's established targets, navigation structure, previews,
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
- Volatile facts that would need synchronized edits — commands, flags, versions, thresholds, module/task enumerations — have one canonical source, preferably the code or configuration itself; other documents point to it and add only why or when it matters. When pointing at a canonical, do not append a breakdown — a parenthetical example list is still an enumeration and drifts. Entrypoints may restate stable safety invariants, and checklists state completion outcomes, not implementation details.
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
| Unit-tested logic (Api clients, shared helpers, Repositories, DataSources, UseCases, the MVI base, feature ViewModels) | `./gradlew :<module>:testAndroidHostTest` (module list canonical in `.github/workflows/app-test.yml`; CI runs them) |
| `shared:model` models or serializers | `./gradlew :shared:model:jvmTest :shared:model:wasmJsTest :server:test` (both consuming targets plus the server-side wire-shape contract) |
| Compose UI or Preview | Feature wasm compile + `./gradlew :app:feature:<name>:compileAndroidMain` |
| Core module or cross-module API | Compile every directly affected consumer |
| Navigation, DI, Gradle, or app wiring | Production client build from `.claude/rules/gradle.md` — Development And Packaging Commands |
| Server Kotlin | `./gradlew :server:test` (compiles and runs the server test suite) |
| `:template` golden sources | `scripts/instantiate_destination.sh --check-sync` + `./gradlew :template:compileKotlinWasmJs :template:compileAndroidMain :test:conventions:test` |
| Convention checks (`test/conventions`) | `./gradlew :test:conventions:test` |
| Custom detekt rules (`detekt-rules/`) | `./gradlew :detekt-rules:test` + the detekt procedure in `.claude/rules/gradle.md` |
| Formatting or lint-sensitive Kotlin | The detekt procedure in `.claude/rules/gradle.md` |
| User-visible wasm UI | Production build and, when practical, the browser smoke test (`.claude/rules/ui-implementation.md`) |
| E2E test infra (`test/tags`, `test/e2e`) | Compile and, when behavior changed, run it per `.claude/rules/ui-testing.md` — Running |

- Use the fully qualified client development-server command from `.claude/rules/gradle.md`; an
  unqualified task can start a different module's server on the same port.
- Suite conventions live in `.claude/rules/server-testing.md`, `.claude/rules/app-testing.md`
  (with ViewModel specifics in `.claude/rules/mvi-testing.md`), and
  `.claude/rules/ui-testing.md`. New logic on both the client and `:server` follows TDD per
  `.claude/rules/tdd.md`.
- Do not claim browser behavior was verified when only compilation or static analysis was run; the browser smoke test procedure is `.claude/rules/ui-implementation.md` — Browser Smoke Test (canonical home).
- Development and packaging commands: `.claude/rules/gradle.md` — Development And Packaging
  Commands.

## Before Handing Off

- Review the final diff for accidental or unrelated changes.
- Verify before asserting: check API existence and behavior against the resolved dependency version or official sources; confirm the running build actually contains the change before diagnosing from runtime observations; distinguish live data from fallbacks before declaring end-to-end success; separate observation from speculation when reporting.
- When a skill step names the independent review lane, it maps to the `rules-reviewer` and `code-reviewer` agents run independently (Codex wrappers: `rules_reviewer` / `code_reviewer`); a cross-model reviewer exists only on Claude Code (the `codex-review` skill).
- Run the narrowest relevant validation, expanding to broader checks for cross-module or release-impacting changes.
- Report what changed, what was validated, and anything not validated.

## Safety And Maintenance

- Never expose secrets, credentials, tokens, signing material, or machine-specific configuration.
- The Android target has two roles only — Preview rendering and client unit-test host runs:
  androidMain actuals may be no-op or no-network stubs, and must never add Android runtime
  features or network calls.
- Declare all dependencies in `gradle/libs.versions.toml` and reference them via the version catalog, including inside convention plugins (`libs.findLibrary(...)`). Do NOT use the deprecated `compose.dependencies.*` Gradle accessors — specify artifacts directly.
- Prefer the existing `kei_1111.*` convention plugins over ad hoc Gradle configuration; their
  source directory is canonical in `.claude/rules/gradle.md` — Convention Plugins.
- Do not add heavy dependencies without approval.
- Do not rewrite large areas, rename public APIs, or move code across modules unless the task requires it.
- Never discard or overwrite unrelated working-tree changes.
- When generated templates or docs disagree with current source code, the source wins (subject to Instruction Priority above).
- Keep `AGENTS.md` and this file updated together when agent-level instructions change.

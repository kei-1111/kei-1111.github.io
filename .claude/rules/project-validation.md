# Project Validation

This project's validation profile. Fixed-name contract: shared skills and agents reference
`.claude/rules/project-validation.md` by this exact name in every repository; each project
defines its own content here.

## Validation By Change Type

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

## Notes

- Use the fully qualified client development-server command from `.claude/rules/gradle.md`; an
  unqualified task can start a different module's server on the same port.
- Suite conventions live in `.claude/rules/server-testing.md`, `.claude/rules/app-testing.md`
  (with ViewModel specifics in `.claude/rules/mvi-testing.md`), and
  `.claude/rules/ui-testing.md`. New logic on both the client and `:server` follows TDD per
  `.claude/rules/tdd.md`.
- Do not claim browser behavior was verified when only compilation or static analysis was run; the browser smoke test procedure is `.claude/rules/ui-implementation.md` — Browser Smoke Test (canonical home).
- Development and packaging commands: `.claude/rules/gradle.md` — Development And Packaging
  Commands.

# Working Agreement (Project)

Project-specific seams of `.claude/rules/working-agreement.md` (the shared core).

- Instruction Priority step 4 (architecture documents) resolves to `docs/ArchitectureOverview.md` and `docs/ModuleOverview.md`
- Preserve this project's established targets, navigation structure, previews, dispatchers, and resources
- The Android target has two roles only — Preview rendering and client unit-test host runs: androidMain actuals may be no-op or no-network stubs, and must never add Android runtime features or network calls
- Declare all dependencies in `gradle/libs.versions.toml` and reference them via the version catalog, including inside convention plugins (`libs.findLibrary(...)`). Do NOT use the deprecated `compose.dependencies.*` Gradle accessors — specify artifacts directly
- Prefer the existing `kei_1111.*` convention plugins over ad hoc Gradle configuration; their source directory is canonical in `.claude/rules/gradle.md` — Convention Plugins

# AGENTS.md — shared/

Rules for the client/server JSON contract (`shared/model`). The root `AGENTS.md` still applies.

- Wire-compatibility rules are canonical in the `GitHubProfile.kt` KDoc — client (GitHub Pages) and server (Cloud Run) deploy independently, so read it before changing any `@Serializable` model: fields are added only with defaults; `@SerialName` renames, field/enum deletions, and type changes are wire-breaking.
- `SharedModelContractTest` (`:server`) pins the serialized shape — keep it green, and update it in the same change as any deliberate contract change.
- The module's commonTest runs on the consuming targets: `./gradlew :shared:model:jvmTest :shared:model:wasmJsTest`.

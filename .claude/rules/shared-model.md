---
paths:
  - "shared/model/**/*.kt"
---

# Shared Wire Contract

Compatibility rules for the client/server JSON contract are canonical in the
`GitHubProfile.kt` KDoc — read it before changing any `@Serializable` model, and keep
`:server`'s `SharedModelContractTest` green in the same change. Test conventions:
`.claude/rules/app-testing.md` (the suite runs via `:shared:model:jvmTest` /
`:shared:model:wasmJsTest`).

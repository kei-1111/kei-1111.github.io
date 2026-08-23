---
paths:
  - "test/conventions/**"
  - "detekt-rules/**"
---

# Conventions Suite Authoring

Verified traps when writing custom Konsist checks (`test/conventions`) and detekt rules (`detekt-rules`):

- A string- or variable-name-based check loses to type resolution: matching call text like `*UseCase(` misses a correctly typed value under another name. Prefer resolved types where the API offers them; where only text is available, keep the pattern anchored to the enforced declaration shape and cover the gap with a rule test.
- A composition such as `combine(...)` must be checked element by element — a substring match over the whole call text passes when only one of several combined flows is protected.
- Konsist parent/type names carry generic arguments — compare with `name.substringBefore('<')` (see `ConventionsSupport.kt`), or a generic supertype silently never matches.
- Convention-check Gradle tasks set `outputs.upToDateWhen { false }` (`test/conventions/build.gradle.kts`) — without it a cached green run hides new violations.

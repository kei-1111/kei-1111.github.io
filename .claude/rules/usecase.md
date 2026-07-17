---
paths:
  - "core/domain/**/*.kt"
---

# UseCase Implementation Patterns

## Structure

- Public interface + `internal` Impl in the **same file**; the only public method is `operator fun invoke(...)`.
- Same Metro annotation trio and order as Repository impls: `@ContributesBinding(AppScope::class)`, `@SingleIn(AppScope::class)`, `@Inject` (see `.claude/rules/data-layer.md`).
- A thin wrapper over **exactly one** Repository call — no branching, no combining.
- **Always append `.distinctUntilChanged()`** to the Repository flow for `Get`-style UseCases — a KEI-specific rule protecting the ViewModel from redundant recompositions if a Repository ever starts re-emitting.

Reference: `core/domain/src/commonMain/kotlin/.../usecase/GetProfileUseCase.kt` and `GetContributionsUseCase.kt` — currently the only two UseCases.

## Principles

1. Single responsibility — each UseCase does exactly one thing; stateless.
2. No inter-UseCase dependencies — a UseCase never calls another UseCase; combine results in the ViewModel instead.
3. No platform classes (e.g. `Context`) in a UseCase's signature or body.
4. Allowed dependency: exactly one Repository. Prohibited: other UseCases, DataSource/Repository implementation details, platform classes.

## When to Create a UseCase

KEI creates a UseCase for **every** Repository read, including trivial pass-throughs like `GetProfileUseCase` — required by the `feature → core:domain → core:data` layering rule (a feature module has no Gradle dependency on `core:data` at all; see `.claude/rules/data-layer.md`). Do not skip the UseCase layer "because it's just a pass-through."

See also: `.claude/rules/error-handling.md` for how the ViewModel wraps a UseCase's `Flow` with `.asResult()`.

---
paths:
  - "core/domain/**/*.kt"
---

# UseCase Implementation Patterns

## Basic Structure

Define the interface and its implementation class in the **same file**.

| Type | Modifier |
|---|---|
| Interface | public |
| Implementation | `internal` |

Currently exactly two UseCases exist, both under `core/domain/src/commonMain/kotlin/.../usecase/`:

| UseCase | Signature | Delegates to |
|---|---|---|
| `GetProfileUseCase` | `operator fun invoke(): Flow<GitHubProfile>` | `ProfileRepository.profile` |
| `GetContributionsUseCase` | `operator fun invoke(user: String): Flow<ContributionCalendar>` | `ContributionsRepository.getContributions(user)` |

**Example**: `core/domain/src/commonMain/kotlin/.../usecase/GetProfileUseCase.kt`

```kt
interface GetProfileUseCase {
    operator fun invoke(): Flow<GitHubProfile>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetProfileUseCaseImpl(
    private val profileRepository: ProfileRepository,
) : GetProfileUseCase {
    override fun invoke(): Flow<GitHubProfile> =
        profileRepository.profile
            .distinctUntilChanged()
}
```

## Pattern

- Same annotation trio and order as Repository impls: `@ContributesBinding(AppScope::class)`,
  `@SingleIn(AppScope::class)`, `@Inject` (see `.claude/rules/data-layer.md`)
- A thin wrapper over **exactly one** Repository call — no branching, no combining
- The only public method is `operator fun invoke(...)`
- **Always append `.distinctUntilChanged()`** to the Repository flow for `Get`-style UseCases — this is a
  KEI-specific rule (both existing UseCases do this; it protects the ViewModel from redundant
  recompositions if a Repository ever starts re-emitting).

## Principles

1. **Single responsibility** — each UseCase does exactly one thing
2. **Stateless** — no mutable data held across calls
3. **Only public method is `invoke()`** — a second public method signals a responsibility split
4. **No inter-UseCase dependencies** — a UseCase never calls another UseCase; combine multiple UseCase
   results in the ViewModel instead
5. **No platform classes** — no `Context`, no platform-specific types in a UseCase's signature or body

## Dependencies

| Allowed | Prohibited |
|---|---|
| Repository (`core:data`, exactly one per UseCase) | Other UseCases |
| — | DataSource / Repository implementation details |
| — | Platform classes (e.g. `Context`) |

## When to Create a UseCase

KEI creates a UseCase for **every** Repository read, including trivial pass-throughs like
`GetProfileUseCase`, for consistency with the `feature → core:domain → core:data` layering rule (a
`feature` module has no Gradle dependency on `core:data` at all — see `.claude/rules/data-layer.md`). Do
not skip the UseCase layer "because it's just a pass-through."

See also: `.claude/rules/error-handling.md` for how the ViewModel wraps a UseCase's `Flow` with
`.asResult()`.

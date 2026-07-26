---
paths:
  - "server/src/main/**"
---

# Server Implementation

Ktor (CIO) JVM server in `server/`, deployed to Cloud Run. Test conventions: `.claude/rules/server-testing.md`.

## Layer Responsibilities

- `routing/*Routes.kt` — HTTP translation only, no policy: call the service, map its result to a response (e.g. `contributions` maps a `null` calendar straight to `503` because the client absorbs it with its error + retry UI).
- `service/*Service.kt` — owns fallback and cache policy: wraps a `TtlCache<T>` around a `GitHubClient` fetch and decides what a miss means (`ProfileService` falls back to `DefaultGitHubProfile`; `ContributionsService` returns `null`).
- `client/GitHubClient.kt` (+ `GitHub*Source.kt`) — owns the GitHub GraphQL API call and its (de)serialization, folding every failure (non-200, GraphQL `errors`, exception, missing token) into `null`.

## TtlCache (`util/TtlCache.kt`)

Caches successful values only, under the `null = failure` contract — keep that contract for new usages. Semantics: single-flight via `Mutex` (concurrent misses share one fetch), stale-if-error (a failed refetch after TTL expiry returns the last successful value), and `retryIntervalMillis` suppressing refetch attempts after a failure (applies to failures only — normal TTL refresh is unaffected).

## Coroutine Cancellation

When catching broadly around suspend I/O to fold failures into a fallback, call `currentCoroutineContext().ensureActive()` before swallowing the exception (see `GitHubClient.execute`) — otherwise a cancelled request looks like a normal API failure.

## Test Seam

`Application.configureApplication(gitHubClient)` is the wiring entry point tests call directly, injecting `GitHubClient(token, MockEngine { ... })` via the engine-accepting constructor inside Ktor's `testApplication` (reference: `server/src/test/.../routing/ApiRoutesTest.kt`).

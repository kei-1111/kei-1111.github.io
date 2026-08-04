---
paths:
  - "server/src/main/**"
---

# Server Implementation

Ktor (CIO) JVM server in `server/`, deployed to Cloud Run. Test conventions: `.claude/rules/server-testing.md`.

## Layer Responsibilities

- `routing/*Routes.kt` — HTTP translation only, no policy: call the service, map its result to a response (e.g. `contributions` maps a `null` calendar straight to `503` because the client absorbs it with its error + retry UI).
- `service/*Service.kt` — owns fallback and cache policy: wraps a `TtlCache<T>` around a `GitHubClient` fetch and decides what a miss means (`ProfileService` falls back to `DefaultGitHubProfile`; `ContributionsService` and `IssuesService` return `null`).
- `client/GitHubClient.kt` (+ `GitHub*Source.kt`) — owns the GitHub GraphQL API call and its (de)serialization, folding every failure (non-200, GraphQL `errors`, exception, missing token) into `null`.

## Plugins (`plugins/`)

- Cross-cutting installs live in `plugins/`, one file per concern (CORS — allowlist plus
  `DEV_CORS_HOSTS` for local hosts, rate limiting, StatusPages, Monitoring, Serialization);
  wire a new concern there, not inside routing.
- Rate limiting: API routes are registered INSIDE the `rateLimit(ApiRateLimiterName) { }`
  block in `Application.kt` — a new route placed outside it (like the deliberate `/health`)
  ships unprotected. The limit and the client-IP key (the `X-Forwarded-For` tail) are
  canonical in `RateLimit.kt`; clients must tolerate 429s.

## TtlCache (`util/TtlCache.kt`)

Caches successful values only, under the `null = failure` contract — keep that contract for new usages. Semantics: single-flight via `Mutex` (concurrent misses share one fetch), stale-if-error (a failed refetch after TTL expiry returns the last successful value), and `retryIntervalMillis` suppressing refetch attempts after a failure (applies to failures only — normal TTL refresh is unaffected).

## Coroutine Cancellation

When catching broadly around suspend I/O to fold failures into a fallback, call `currentCoroutineContext().ensureActive()` before swallowing the exception (see `GitHubClient.execute`) — otherwise a cancelled request looks like a normal API failure.

## Test Seam

`Application.configureApplication(gitHubClient)` is the wiring entry point tests call directly, injecting `GitHubClient(token, MockEngine { ... })` via the engine-accepting constructor inside Ktor's `testApplication` (reference: `server/src/test/.../routing/ApiRoutesTest.kt`).

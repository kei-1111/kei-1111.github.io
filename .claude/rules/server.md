---
paths:
  - "server/src/main/**"
---

# Server Implementation

Ktor (CIO) JVM server in `server/`, deployed to Cloud Run. Test conventions: `.claude/rules/server-testing.md`.

## Layer Responsibilities

- `routing/*Routes.kt` — HTTP translation only, no policy: call the service and map its result to
  a response; a service that returns `null` becomes 503. Exact status mappings are canonical in the
  route source; the client absorbs unavailable remote data with its error and retry UI. Each file
  declares exactly one `internal fun Route.xxx(service)` extension.
- `service/*Service.kt` — owns cache policy: wraps a `TtlCache<T>` around a client fetch (`GitHubClient` / `PublishedContentClient`) and decides what a miss means. The server keeps no content of its own — every service returns `null` when its source is unavailable. `ProfileService` composes the published profile with the GitHub statistics and leaves those statistics absent when GitHub is unreachable; `WorksService` / `ReadmeService` / `TerminalCommandsService` serve their published document or nothing.
- `client/GitHubClient.kt` (+ `GitHub*Source.kt`) — owns the GitHub GraphQL API call and its (de)serialization, folding operational failures (non-200, GraphQL `errors`, non-cancellation exception, missing token) into `null`; coroutine cancellation propagates. `client/PublishedContentClient.kt` (+ `PublishedContent.kt`) maps the admin schema to contract models — except the published profile, which stays an admin-schema value that `ProfileService` composes with the GitHub statistics; a missing object is `Missing` while an operational failure is `null` (stale-if-error), and both leave the service without content to serve.

Each `client/GitHub*Source.kt` holds one `*_QUERY` constant and one `fetchXxx` extension on
`GitHubClient`. `client/` DTOs never property-annotate `@SerialName` — wire names are pinned
property-by-property in `shared/model` only (see `.claude/rules/shared-model.md`).
`kotlinx-datetime` stays out of `server` and `shared/model`.

## Plugins (`plugins/`)

- Cross-cutting installs live in `plugins/`, one file per concern, each declaring exactly one
  `fun Application.configureXxx()`; the directory is the canonical
  list. Wire a new concern there, not inside routing.
- Rate limiting: API routes are registered INSIDE the `rateLimit(ApiRateLimiterName) { }`
  block in `Application.kt` — a new route placed outside it (like the deliberate `/health`)
  ships unprotected. The limit and the client-IP key (the `X-Forwarded-For` tail) are
  canonical in `RateLimit.kt`; clients must tolerate rate-limit responses.

## TtlCache (`util/TtlCache.kt`)

Caches successful values only, under the `null = failure` contract — keep that contract for new usages. Every instantiation passes an explicit `name`. Semantics: single-flight via `Mutex` (concurrent misses share one fetch), stale-if-error (a failed refetch after TTL expiry returns the last successful value), and `retryIntervalMillis` suppressing refetch attempts after a failure (applies to failures only — normal TTL refresh is unaffected).

## Coroutine Cancellation

When catching broadly around suspend I/O to fold failures into a fallback, call `currentCoroutineContext().ensureActive()` before swallowing the exception (see `GitHubClient.execute`) — otherwise a cancelled request looks like a normal API failure.

## Test Seam

`Application.configureApplication(gitHubClient)` is the wiring entry point tests call directly, injecting `GitHubClient(token, MockEngine { ... })` via the engine-accepting constructor inside Ktor's `testApplication` (reference: `server/src/test/.../routing/ApiRoutesTest.kt`).

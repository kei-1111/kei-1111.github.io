package io.github.kei_1111.server.client

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val TOKEN = "test-token"

private const val PROFILE_RESPONSE = """
{"data":{"user":{
  "followers":{"totalCount":16},
  "following":{"totalCount":21},
  "repositories":{"totalCount":32,"nodes":[
    {"languages":{"edges":[
      {"size":700,"node":{"name":"Kotlin","color":null}},
      {"size":200,"node":{"name":"TypeScript","color":"#3178C6"}}
    ]}},
    {"languages":{"edges":[
      {"size":300,"node":{"name":"Kotlin","color":"#A97BFF"}},
      {"size":100,"node":{"name":"Shell","color":"#89e051"}}
    ]}}
  ]},
  "starredRepositories":{"totalCount":41},
  "pinnedItems":{"nodes":[
    {
      "name":"kei-1111.github.io",
      "description":"GitHub profile description",
      "url":"https://github.com/kei-1111/kei-1111.github.io",
      "stargazerCount":0,
      "primaryLanguage":{"name":"Kotlin"}
    },
    {
      "name":"unregistered-repo",
      "description":null,
      "url":"https://github.com/kei-1111/unregistered-repo",
      "stargazerCount":2,
      "primaryLanguage":null
    }
  ]}
}}}
"""

private val EXPECTED_PROFILE_STATS_QUERY = """
    query(${'$'}login: String!) {
      user(login: ${'$'}login) {
        followers { totalCount }
        following { totalCount }
        repositories(first: 100, ownerAffiliations: [OWNER], privacy: PUBLIC) {
          totalCount
          nodes {
            languages(first: 20) {
              edges {
                size
                node { name color }
              }
            }
          }
        }
        starredRepositories { totalCount }
        pinnedItems(first: 6, types: [REPOSITORY]) {
          nodes {
            ... on Repository {
              name
              description
              url
              stargazerCount
              primaryLanguage { name }
            }
          }
        }
      }
    }
""".trimIndent()

private fun contributionsResponse(level: String = "FOURTH_QUARTILE") = """
{"data":{"user":{"contributionsCollection":{"contributionCalendar":{
  "totalContributions":5,
  "weeks":[
    {"contributionDays":[
      {"date":"2025-07-13","contributionCount":0,"contributionLevel":"NONE"},
      {"date":"2025-07-14","contributionCount":1,"contributionLevel":"FIRST_QUARTILE"}
    ]},
    {"contributionDays":[
      {"date":"2025-07-15","contributionCount":4,"contributionLevel":"$level"}
    ]}
  ]
}}}}}
"""

private const val ISSUES_RESPONSE = """
{"data":{"repository":{"issues":{
  "totalCount":2,
  "nodes":[
    {"number":106,"title":"[Feature]: Add a TODO tool window","url":"https://github.com/kei-1111/kei-1111.github.io/issues/106"},
    {"number":24,"title":"作品ページの追加（作品 API + クライアント UI）","url":"https://github.com/kei-1111/kei-1111.github.io/issues/24"}
  ]
}}}}
"""

private const val CHANGELOG_RESPONSE = """
{"data":{"repository":{"pullRequests":{
  "nodes":[
    {
      "number":204,
      "title":"[Feature]: Add changelog backend",
      "url":"https://github.com/kei-1111/kei-1111.github.io/pull/204",
      "headRefName":"feature/204",
      "mergedAt":"2026-08-08T01:00:00Z",
      "author":{"login":"kei-1111"}
    },
    {
      "number":205,
      "title":"Keep the original title",
      "url":"https://github.com/kei-1111/kei-1111.github.io/pull/205",
      "headRefName":"feature/205",
      "mergedAt":"2026-08-09T02:00:00Z",
      "author":null
    },
    {
      "number":203,
      "title":"[Fix]: Ignore unmerged data",
      "url":"https://github.com/kei-1111/kei-1111.github.io/pull/203",
      "headRefName":"fix/203",
      "mergedAt":null,
      "author":{"login":"kei-1111"}
    }
  ]
}}}}
"""

private val EXPECTED_MERGED_PULL_REQUESTS_QUERY = """
    query(${'$'}owner: String!, ${'$'}name: String!) {
      repository(owner: ${'$'}owner, name: ${'$'}name) {
        pullRequests(
          states: MERGED,
          baseRefName: "main",
          first: 100,
          orderBy: {field: CREATED_AT, direction: DESC}
        ) {
          nodes { number title url headRefName mergedAt author { login } }
        }
      }
    }
""".trimIndent()

private fun jsonEngine(body: String) = MockEngine {
    respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
}

class GitHubSourceTest {

    @Test
    fun executeDoesNotResumeNormallyAfterCallerCancellation() = runBlocking {
        val requestStarted = CompletableDeferred<Unit>()
        val engine = MockEngine {
            requestStarted.complete(Unit)
            awaitCancellation()
        }
        var completedNormally = false
        GitHubClient(TOKEN, engine).use { client ->
            val request = launch {
                client.fetchProfileStats()
                completedNormally = true
            }
            requestStarted.await()
            request.cancelAndJoin()
        }

        assertFalse(completedNormally)
    }

    @Test
    fun fetchProfileStatsMapsASuccessfulResponse() = runBlocking {
        val engine = jsonEngine(PROFILE_RESPONSE)
        GitHubClient(TOKEN, engine).use { client ->
            val stats = client.fetchProfileStats()

            assertEquals(16, stats?.followers)
            assertEquals(21, stats?.following)
            assertEquals(32, stats?.repos)
            // totalStars は starredRepositories.totalCount(スターを付けた数)であること。
            assertEquals(41, stats?.totalStars)
            assertEquals(
                listOf(
                    LanguageBytes(name = "Kotlin", color = "#A97BFF", size = 1_000),
                    LanguageBytes(name = "TypeScript", color = "#3178C6", size = 200),
                    LanguageBytes(name = "Shell", color = "#89e051", size = 100),
                ),
                stats?.languageSizes,
            )
            assertEquals(
                listOf(
                    PinnedRepoSource(
                        name = "kei-1111.github.io",
                        description = "GitHub profile description",
                        url = "https://github.com/kei-1111/kei-1111.github.io",
                        stars = 0,
                        languageName = "Kotlin",
                    ),
                    PinnedRepoSource(
                        name = "unregistered-repo",
                        description = null,
                        url = "https://github.com/kei-1111/unregistered-repo",
                        stars = 2,
                        languageName = null,
                    ),
                ),
                stats?.pinnedRepos,
            )
        }
    }

    @Test
    fun fetchProfileStatsSendsTheExpectedGraphQlRequest() = runBlocking {
        val engine = jsonEngine(PROFILE_RESPONSE)
        GitHubClient(TOKEN, engine).use { client ->
            client.fetchProfileStats()

            val request = engine.requestHistory.single()
            val body = Json.decodeFromString<GraphQlRequest>((request.body as TextContent).text)
            assertEquals(GitHubClient.GRAPHQL_ENDPOINT, request.url.toString())
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("Bearer $TOKEN", request.headers[HttpHeaders.Authorization])
            assertEquals(EXPECTED_PROFILE_STATS_QUERY, body.query)
            assertEquals(mapOf("login" to PROFILE_LOGIN), body.variables)
        }
    }

    @Test
    fun fetchContributionsMapsASuccessfulResponse() = runBlocking {
        val engine = jsonEngine(contributionsResponse())
        GitHubClient(TOKEN, engine).use { client ->
            val calendar = client.fetchContributions()
            val days = calendar?.days.orEmpty()

            assertEquals(5, calendar?.totalLastYear)
            assertEquals(3, days.size)
            // weeks を返却順に flatten するので古い日付が先頭に来る。
            assertEquals(listOf("2025-07-13", "2025-07-14", "2025-07-15"), days.map { it.date })
            assertEquals(listOf(0, 1, 4), days.map { it.level })
            assertEquals(listOf(0, 1, 4), days.map { it.count })
            assertEquals(calendar?.totalLastYear, days.sumOf { it.count })
        }
    }

    @Test
    fun fetchOpenIssuesMapsASuccessfulResponse() = runBlocking {
        val engine = jsonEngine(ISSUES_RESPONSE)
        GitHubClient(TOKEN, engine).use { client ->
            val issues = client.fetchOpenIssues()

            assertEquals(2, issues?.totalCount)
            assertEquals(listOf(106, 24), issues?.issues?.map { it.number })
            assertEquals("Feature", issues?.issues?.first()?.type)
            assertEquals("Add a TODO tool window", issues?.issues?.first()?.title)
            assertEquals("https://github.com/kei-1111/kei-1111.github.io/issues/106", issues?.issues?.first()?.url)
        }
    }

    @Test
    fun fetchMergedPullRequestsMapsASuccessfulResponse() = runBlocking {
        val engine = jsonEngine(CHANGELOG_RESPONSE)
        GitHubClient(TOKEN, engine).use { client ->
            val changelog = client.fetchMergedPullRequests()

            assertEquals(listOf(205, 204), changelog?.pullRequests?.map { it.number })
            assertEquals("Keep the original title", changelog?.pullRequests?.first()?.title)
            assertNull(changelog?.pullRequests?.first()?.type)
            assertNull(changelog?.pullRequests?.first()?.author)
            assertEquals("kei-1111", changelog?.pullRequests?.last()?.author)
            assertEquals("Feature", changelog?.pullRequests?.last()?.type)
            assertEquals("Add changelog backend", changelog?.pullRequests?.last()?.title)
            assertEquals("feature/204", changelog?.pullRequests?.last()?.headRefName)
            assertEquals("2026-08-08T01:00:00Z", changelog?.pullRequests?.last()?.mergedAt)
            assertEquals(
                "https://github.com/kei-1111/kei-1111.github.io/pull/204",
                changelog?.pullRequests?.last()?.url,
            )
        }
    }

    @Test
    fun fetchProfileStatsReturnsNullOnHttpError() = runBlocking {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchProfileStats())
        }
    }

    @Test
    fun fetchMergedPullRequestsReturnsNullOnHttpError() = runBlocking {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchMergedPullRequests())
        }
    }

    @Test
    fun fetchProfileStatsReturnsNullWhenGraphQlReportsErrors() = runBlocking {
        val engine = jsonEngine("""{"errors":[{"message":"Bad credentials"}]}""")
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchProfileStats())
        }
    }

    @Test
    fun fetchMergedPullRequestsReturnsNullWhenGraphQlReportsErrors() = runBlocking {
        val engine = jsonEngine("""{"errors":[{"message":"Bad credentials"}]}""")
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchMergedPullRequests())
        }
    }

    @Test
    fun fetchProfileStatsReturnsNullWhenTheUserIsNull() = runBlocking {
        // HTTP 200 + errors なしでも user が null のケース(アカウント改名やスコープ不足)。
        val engine = jsonEngine("""{"data":{"user":null}}""")
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchProfileStats())
        }
    }

    @Test
    fun fetchContributionsSendsTheExpectedGraphQlRequest() = runBlocking {
        val engine = jsonEngine(contributionsResponse())
        GitHubClient(TOKEN, engine).use { client ->
            client.fetchContributions()

            val request = engine.requestHistory.single()
            val body = Json.decodeFromString<GraphQlRequest>((request.body as TextContent).text)
            val variables = body.variables
            assertEquals(GitHubClient.GRAPHQL_ENDPOINT, request.url.toString())
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("Bearer $TOKEN", request.headers[HttpHeaders.Authorization])
            assertEquals(CONTRIBUTIONS_QUERY, body.query)
            assertEquals(PROFILE_LOGIN, variables["login"])
            assertEquals(setOf("login", "from", "to"), variables.keys)
            val from = Instant.parse(variables.getValue("from")).atZone(ZoneOffset.UTC)
            val to = Instant.parse(variables.getValue("to"))

            // ContributionGraph が days の通し index % 7 を曜日の行として描くため、先頭は常に日曜 0 時。
            assertEquals(DayOfWeek.SUNDAY, from.dayOfWeek)
            assertEquals(LocalTime.MIDNIGHT, from.toLocalTime())
            assertTrue(from.toInstant().isBefore(to))
        }
    }

    @Test
    fun fetchOpenIssuesSendsTheExpectedGraphQlRequest() = runBlocking {
        val engine = jsonEngine(ISSUES_RESPONSE)
        GitHubClient(TOKEN, engine).use { client ->
            client.fetchOpenIssues()

            val request = engine.requestHistory.single()
            val body = Json.decodeFromString<GraphQlRequest>((request.body as TextContent).text)
            assertEquals(GitHubClient.GRAPHQL_ENDPOINT, request.url.toString())
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("Bearer $TOKEN", request.headers[HttpHeaders.Authorization])
            assertEquals(OPEN_ISSUES_QUERY, body.query)
            assertEquals(mapOf("owner" to PROFILE_LOGIN, "name" to REPO_NAME), body.variables)
        }
    }

    @Test
    fun fetchMergedPullRequestsSendsTheExpectedGraphQlRequest() = runBlocking {
        val engine = jsonEngine(CHANGELOG_RESPONSE)
        GitHubClient(TOKEN, engine).use { client ->
            client.fetchMergedPullRequests()

            val request = engine.requestHistory.single()
            val body = Json.decodeFromString<GraphQlRequest>((request.body as TextContent).text)
            assertEquals(GitHubClient.GRAPHQL_ENDPOINT, request.url.toString())
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("Bearer $TOKEN", request.headers[HttpHeaders.Authorization])
            assertEquals(EXPECTED_MERGED_PULL_REQUESTS_QUERY, body.query)
            assertEquals(mapOf("owner" to PROFILE_LOGIN, "name" to REPO_NAME), body.variables)
        }
    }

    @Test
    fun fetchContributionsReturnsNullWhenTheUserIsNull() = runBlocking {
        val engine = jsonEngine("""{"data":{"user":null}}""")
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchContributions())
        }
    }

    @Test
    fun fetchOpenIssuesKeepsTheTitleWhenNoTypePrefix() = runBlocking {
        val engine = jsonEngine(ISSUES_RESPONSE)
        GitHubClient(TOKEN, engine).use { client ->
            val issue = client.fetchOpenIssues()?.issues?.last()

            assertEquals("作品ページの追加（作品 API + クライアント UI）", issue?.title)
            assertNull(issue?.type)
        }
    }

    @Test
    fun fetchOpenIssuesParsesAConventionalCommitsTitle() = runBlocking {
        val engine = jsonEngine(
            """
            {"data":{"repository":{"issues":{
              "totalCount":1,
              "nodes":[
                {"number":239,"title":"fix(server): parse Conventional Commits Issue and PR titles","url":"https://github.com/kei-1111/kei-1111.github.io/issues/239"}
              ]
            }}}}
            """,
        )
        GitHubClient(TOKEN, engine).use { client ->
            val issue = client.fetchOpenIssues()?.issues?.single()

            assertEquals("fix(server)", issue?.type)
            assertEquals("parse Conventional Commits Issue and PR titles", issue?.title)
        }
    }

    @Test
    fun fetchOpenIssuesReturnsNullWhenTheRepositoryIsNull() = runBlocking {
        val engine = jsonEngine("""{"data":{"repository":null}}""")
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchOpenIssues())
        }
    }

    @Test
    fun fetchMergedPullRequestsParsesAConventionalCommitsTitle() = runBlocking {
        val engine = jsonEngine(
            """
            {"data":{"repository":{"pullRequests":{
              "nodes":[
                {
                  "number":240,
                  "title":"chore: adopt Conventional Commits format for Issue and PR titles",
                  "url":"https://github.com/kei-1111/kei-1111.github.io/pull/240",
                  "headRefName":"chore/#238",
                  "mergedAt":"2026-08-24T01:00:00Z",
                  "author":{"login":"kei-1111"}
                }
              ]
            }}}}
            """,
        )
        GitHubClient(TOKEN, engine).use { client ->
            val pullRequest = client.fetchMergedPullRequests()?.pullRequests?.single()

            assertEquals("chore", pullRequest?.type)
            assertEquals("adopt Conventional Commits format for Issue and PR titles", pullRequest?.title)
        }
    }

    @Test
    fun fetchMergedPullRequestsReturnsNullWhenTheRepositoryIsNull() = runBlocking {
        val engine = jsonEngine("""{"data":{"repository":null}}""")
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchMergedPullRequests())
        }
    }

    @Test
    fun fetchProfileStatsReturnsNullOnMalformedJson() = runBlocking {
        val engine = jsonEngine("not json at all")
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchProfileStats())
        }
    }

    @Test
    fun fetchMergedPullRequestsReturnsNullOnMalformedJson() = runBlocking {
        val engine = jsonEngine("not json at all")
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchMergedPullRequests())
        }
    }

    @Test
    fun fetchContributionsReturnsNullOnAnUnknownContributionLevel() = runBlocking {
        val engine = jsonEngine(contributionsResponse(level = "FIFTH_QUARTILE"))
        GitHubClient(TOKEN, engine).use { client ->
            // 未知の level は黙って 0 に畳まず、取得全体を失敗にする。
            assertNull(client.fetchContributions())
        }
    }

    @Test
    fun doesNotCallTheApiWhenTheTokenIsNull() = runBlocking {
        val engine = jsonEngine(PROFILE_RESPONSE)
        GitHubClient(null, engine).use { client ->
            assertNull(client.fetchProfileStats())
            assertNull(client.fetchContributions())
            assertNull(client.fetchOpenIssues())
            assertNull(client.fetchMergedPullRequests())

            assertTrue(engine.requestHistory.isEmpty(), "no HTTP request should be issued without a token")
        }
    }

    @Test
    fun closeIsIdempotent() {
        val client = GitHubClient(TOKEN, jsonEngine(PROFILE_RESPONSE))

        client.close()
        client.close()
    }
}

package io.github.kei_1111.server.client

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val TOKEN = "test-token"

private const val PROFILE_RESPONSE = """
{"data":{"user":{
  "followers":{"totalCount":16},
  "following":{"totalCount":21},
  "repositories":{"totalCount":32},
  "starredRepositories":{"totalCount":41}
}}}
"""

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

private fun jsonEngine(body: String) = MockEngine {
    respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
}

class GitHubSourceTest {

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
    fun fetchProfileStatsReturnsNullOnHttpError() = runBlocking {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchProfileStats())
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
    fun fetchProfileStatsReturnsNullWhenTheUserIsNull() = runBlocking {
        // HTTP 200 + errors なしでも user が null のケース(アカウント改名やスコープ不足)。
        val engine = jsonEngine("""{"data":{"user":null}}""")
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchProfileStats())
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
    fun fetchProfileStatsReturnsNullOnMalformedJson() = runBlocking {
        val engine = jsonEngine("not json at all")
        GitHubClient(TOKEN, engine).use { client ->
            assertNull(client.fetchProfileStats())
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

package io.github.kei_1111.server.routing

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.configureApplication
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

private const val TOKEN = "test-token"

// 静的フォールバック(content/ProfileContent.kt)の統計値。GitHub 取得失敗時はこの値がそのまま配信される。
private const val FALLBACK_FOLLOWERS = 15
private const val FALLBACK_FOLLOWING = 25
private const val FALLBACK_REPOS = 32
private const val FALLBACK_TOTAL_STARS = 41

private const val LIVE_FOLLOWERS = 16
private const val LIVE_FOLLOWING = 21
private const val LIVE_REPOS = 30
private const val LIVE_TOTAL_STARS = 41

private const val PROFILE_RESPONSE = """
{"data":{"user":{
  "followers":{"totalCount":16},
  "following":{"totalCount":21},
  "repositories":{"totalCount":30},
  "starredRepositories":{"totalCount":41}
}}}
"""

private const val CONTRIBUTIONS_RESPONSE = """
{"data":{"user":{"contributionsCollection":{"contributionCalendar":{
  "totalContributions":5,
  "weeks":[{"contributionDays":[
    {"date":"2025-07-13","contributionCount":1,"contributionLevel":"FIRST_QUARTILE"},
    {"date":"2025-07-14","contributionCount":4,"contributionLevel":"FOURTH_QUARTILE"}
  ]}]
}}}}}
"""

private val json = Json { ignoreUnknownKeys = true }

private fun jsonEngine(body: String) = MockEngine {
    respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
}

private fun failingEngine() = MockEngine { respondError(HttpStatusCode.InternalServerError) }

class ApiRoutesTest {

    @Test
    fun healthzReturnsOk() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        val response = client.get("/healthz")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }

    @Test
    fun profileMergesLiveStatsWhenGitHubSucceeds() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, jsonEngine(PROFILE_RESPONSE))) }

        val response = client.get("/api/profile")
        val profile = json.decodeFromString<GitHubProfile>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(LIVE_FOLLOWERS, profile.followers)
        assertEquals(LIVE_FOLLOWING, profile.following)
        assertEquals(LIVE_REPOS, profile.repos)
        assertEquals(LIVE_TOTAL_STARS, profile.totalStars)
        // 静的な自己紹介部分はライブ統計で上書きされない。
        assertEquals("kei-1111", profile.handle)
    }

    @Test
    fun profileServesStaticValuesWhenGitHubFails() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        val response = client.get("/api/profile")
        val profile = json.decodeFromString<GitHubProfile>(response.bodyAsText())

        // 取得に失敗しても 200 + 静的スナップショットを返す(クライアントは常にプロフィールを描画できる)。
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(FALLBACK_FOLLOWERS, profile.followers)
        assertEquals(FALLBACK_FOLLOWING, profile.following)
        assertEquals(FALLBACK_REPOS, profile.repos)
        assertEquals(FALLBACK_TOTAL_STARS, profile.totalStars)
    }

    @Test
    fun contributionsReturnsTheCalendarWhenGitHubSucceeds() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, jsonEngine(CONTRIBUTIONS_RESPONSE))) }

        val response = client.get("/api/contributions")
        val calendar = json.decodeFromString<ContributionCalendar>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(5, calendar.totalLastYear)
        assertEquals(listOf("2025-07-13", "2025-07-14"), calendar.days.map { it.date })
        assertEquals(listOf(1, 4), calendar.days.map { it.level })
    }

    @Test
    fun contributionsReturnsServiceUnavailableWhenGitHubFails() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        val response = client.get("/api/contributions")

        // 取得不能時はクライアント側の FallbackContributions が受け止めるため 503。
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
    }

    @Test
    fun corsAllowsTheProductionOrigin() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        val response = client.get("/api/profile") {
            header(HttpHeaders.Origin, "https://kei-1111.github.io")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            "https://kei-1111.github.io",
            response.headers[HttpHeaders.AccessControlAllowOrigin],
        )
    }

    @Test
    fun corsRejectsAnUnknownOrigin() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        val response = client.get("/api/profile") {
            header(HttpHeaders.Origin, "https://evil.example.com")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}

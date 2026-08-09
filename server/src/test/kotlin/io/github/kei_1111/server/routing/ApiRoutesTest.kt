package io.github.kei_1111.server.routing

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.configureApplication
import io.github.kei_1111.server.content.DefaultWorks
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.RepoLanguage
import io.github.kei_1111.shared.model.Works
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
import kotlinx.collections.immutable.persistentListOf
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
  "repositories":{"totalCount":30,"nodes":[
    {"languages":{"edges":[
      {"size":700,"node":{"name":"Kotlin","color":"#A97BFF"}},
      {"size":200,"node":{"name":"TypeScript","color":"#3178C6"}},
      {"size":100,"node":{"name":"Shell","color":"#89e051"}}
    ]}}
  ]},
  "starredRepositories":{"totalCount":41}
}}}
"""

private const val PROFILE_WITHOUT_LANGUAGES_RESPONSE = """
{"data":{"user":{
  "followers":{"totalCount":16},
  "following":{"totalCount":21},
  "repositories":{"totalCount":30,"nodes":[]},
  "starredRepositories":{"totalCount":41}
}}}
"""

private val FALLBACK_LANGUAGES = persistentListOf(
    LanguageShare(language = RepoLanguage("Kotlin"), share = 0.87f, color = "#A97BFF"),
    LanguageShare(language = RepoLanguage("Swift"), share = 0.10f, color = "#F05138"),
    LanguageShare(language = RepoLanguage("Shell"), share = 0.02f, color = "#89e051"),
)

private const val CONTRIBUTIONS_RESPONSE = """
{"data":{"user":{"contributionsCollection":{"contributionCalendar":{
  "totalContributions":5,
  "weeks":[{"contributionDays":[
    {"date":"2025-07-13","contributionCount":1,"contributionLevel":"FIRST_QUARTILE"},
    {"date":"2025-07-14","contributionCount":4,"contributionLevel":"FOURTH_QUARTILE"}
  ]}]
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
    fun healthReturnsOk() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        val response = client.get("/health")

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
        assertEquals(listOf("Kotlin", "TypeScript", "Shell"), profile.languages.map { it.language.name })
        assertEquals(listOf(0.7f, 0.2f, 0.1f), profile.languages.map { it.share })
        assertEquals(listOf("#A97BFF", "#3178C6", "#89e051"), profile.languages.map { it.color })
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
        assertEquals(FALLBACK_LANGUAGES, profile.languages)
    }

    @Test
    fun profileServesStaticLanguagesWithLiveStatsWhenGitHubHasNoLanguageData() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, jsonEngine(PROFILE_WITHOUT_LANGUAGES_RESPONSE))) }

        val response = client.get("/api/profile")
        val profile = json.decodeFromString<GitHubProfile>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(LIVE_FOLLOWERS, profile.followers)
        assertEquals(LIVE_FOLLOWING, profile.following)
        assertEquals(LIVE_REPOS, profile.repos)
        assertEquals(LIVE_TOTAL_STARS, profile.totalStars)
        assertEquals(FALLBACK_LANGUAGES, profile.languages)
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

        // 取得不能時はクライアント側がエラー表示＋再試行で受け止めるため 503。
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
    }

    @Test
    fun issuesReturnsTheOpenIssuesWhenGitHubSucceeds() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, jsonEngine(ISSUES_RESPONSE))) }

        val response = client.get("/api/issues")
        val issues = json.decodeFromString<GitHubIssues>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, issues.totalCount)
        assertEquals(listOf(106, 24), issues.issues.map { it.number })
        assertEquals("Feature", issues.issues.first().type)
        assertEquals("Add a TODO tool window", issues.issues.first().title)
    }

    @Test
    fun issuesReturnsServiceUnavailableWhenGitHubFails() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        val response = client.get("/api/issues")

        // contributions と同じく静的フォールバックは持たず、クライアント側のエラー表示＋再試行に委ねる。
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
    }

    @Test
    fun worksReturnsTheStaticWorksList() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        val response = client.get("/api/works")
        val works = json.decodeFromString<Works>(response.bodyAsText())

        // works は GitHub API に依存しない静的コンテンツなので、常に 200 + 固定リストを返す。
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(DefaultWorks, works)
        assertEquals(listOf("withmo", "kei-1111-github-io"), works.items.map { it.id })
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

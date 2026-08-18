package io.github.kei_1111.server.routing

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.PublishedReadmeFixture
import io.github.kei_1111.server.client.PublishedTerminalCommandsFixture
import io.github.kei_1111.server.client.PublishedWorksFixture
import io.github.kei_1111.server.client.publishedContentClient
import io.github.kei_1111.server.configureApplication
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.Profile
import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.RepoLanguage
import io.github.kei_1111.shared.model.TerminalTextCommands
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
import kotlin.test.assertNull

private const val TOKEN = "test-token"

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
  "starredRepositories":{"totalCount":41},
  "pinnedItems":{"nodes":[
    {
      "name":"kei-1111.github.io",
      "description":"GitHub description that must be overridden",
      "url":"https://github.com/kei-1111/kei-1111.github.io",
      "stargazerCount":0,
      "primaryLanguage":{"name":"Kotlin"}
    },
    {
      "name":"live-only-repo",
      "description":"Description from GitHub",
      "url":"https://github.com/kei-1111/live-only-repo",
      "stargazerCount":3,
      "primaryLanguage":{"name":"Rust"}
    }
  ]}
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

private const val PROFILE_WITHOUT_PINNED_RESPONSE = """
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
  "starredRepositories":{"totalCount":41},
  "pinnedItems":{"nodes":[]}
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
      "author":{"login":"kei-1111"}
    }
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
    fun profileComposesThePublishedContentWithLiveStats() = testApplication {
        application {
            configureApplication(GitHubClient(TOKEN, jsonEngine(PROFILE_RESPONSE)), publishedContentClient())
        }

        val response = client.get("/api/profile")
        val profile = json.decodeFromString<Profile>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(LIVE_FOLLOWERS, profile.followers)
        assertEquals(LIVE_FOLLOWING, profile.following)
        assertEquals(LIVE_REPOS, profile.repos)
        assertEquals(LIVE_TOTAL_STARS, profile.totalStars)
        assertEquals(listOf("Kotlin", "TypeScript", "Shell"), profile.languages.map { it.language.name })
        assertEquals(listOf(0.7f, 0.2f, 0.1f), profile.languages.map { it.share })
        assertEquals(listOf("#A97BFF", "#3178C6", "#89e051"), profile.languages.map { it.color })
        assertEquals(
            persistentListOf(
                PinnedRepo(
                    name = "kei-1111.github.io",
                    description = LocalizedText(ja = "公開側の説明", en = "Published description"),
                    url = "https://github.com/kei-1111/kei-1111.github.io",
                    language = RepoLanguage("Kotlin"),
                ),
                PinnedRepo(
                    name = "live-only-repo",
                    description = LocalizedText(ja = "Description from GitHub", en = "Description from GitHub"),
                    url = "https://github.com/kei-1111/live-only-repo",
                    language = RepoLanguage("Rust"),
                    stars = 3,
                ),
            ),
            profile.pinnedRepos,
        )
        // handle はサーバーが GitHub へ問い合わせる login そのもので、公開コンテンツからは編集できない。
        assertEquals("kei-1111", profile.handle)
    }

    @Test
    fun profileReturnsServiceUnavailableWhenNothingIsPublished() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, jsonEngine(PROFILE_RESPONSE))) }

        assertEquals(HttpStatusCode.ServiceUnavailable, client.get("/api/profile").status)
    }

    @Test
    fun profileLeavesTheStatisticsAbsentWhenGitHubFails() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine()), publishedContentClient()) }

        val response = client.get("/api/profile")
        val profile = json.decodeFromString<Profile>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertNull(profile.followers)
        assertNull(profile.following)
        assertNull(profile.repos)
        assertNull(profile.totalStars)
        assertEquals(persistentListOf(), profile.pinnedRepos)
        assertEquals(persistentListOf(), profile.languages)
        assertEquals(LocalizedText(ja = "けい", en = "Kei"), profile.name)
    }

    @Test
    fun profileHasNoLanguagesWhenGitHubReportsNone() = testApplication {
        application {
            configureApplication(
                GitHubClient(TOKEN, jsonEngine(PROFILE_WITHOUT_LANGUAGES_RESPONSE)),
                publishedContentClient(),
            )
        }

        val response = client.get("/api/profile")
        val profile = json.decodeFromString<Profile>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(LIVE_FOLLOWERS, profile.followers)
        assertEquals(persistentListOf(), profile.languages)
    }

    @Test
    fun profileHasNoPinnedReposWhenGitHubReportsNone() = testApplication {
        application {
            configureApplication(
                GitHubClient(TOKEN, jsonEngine(PROFILE_WITHOUT_PINNED_RESPONSE)),
                publishedContentClient(),
            )
        }

        val response = client.get("/api/profile")
        val profile = json.decodeFromString<Profile>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(LIVE_FOLLOWERS, profile.followers)
        assertEquals(persistentListOf(), profile.pinnedRepos)
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
    fun changelogReturnsTheMergedPullRequestsWhenGitHubSucceeds() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, jsonEngine(CHANGELOG_RESPONSE))) }

        val response = client.get("/api/changelog")
        val changelog = json.decodeFromString<GitHubChangelog>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(205, 204), changelog.pullRequests.map { it.number })
        assertEquals("Feature", changelog.pullRequests.last().type)
        assertEquals("Add changelog backend", changelog.pullRequests.last().title)
        assertEquals("kei-1111", changelog.pullRequests.last().author)
    }

    @Test
    fun changelogReturnsServiceUnavailableWhenGitHubFails() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        val response = client.get("/api/changelog")

        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
    }

    @Test
    fun worksServesThePublishedList() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine()), publishedContentClient()) }

        val response = client.get("/api/works")
        val works = json.decodeFromString<Works>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(PublishedWorksFixture, works)
    }

    @Test
    fun worksReturnsServiceUnavailableWhenNothingIsPublished() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        assertEquals(HttpStatusCode.ServiceUnavailable, client.get("/api/works").status)
    }

    @Test
    fun readmeServesThePublishedDocument() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine()), publishedContentClient()) }

        val response = client.get("/api/readme")
        val readme = json.decodeFromString<Readme>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(PublishedReadmeFixture, readme)
        assertEquals(
            MarkdownBlock.Heading(
                level = 1,
                inlines = persistentListOf(MarkdownInline.PlainText("kei-1111.github.io")),
            ),
            readme.ja.first(),
        )
    }

    @Test
    fun readmeReturnsServiceUnavailableWhenNothingIsPublished() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        assertEquals(HttpStatusCode.ServiceUnavailable, client.get("/api/readme").status)
    }

    @Test
    fun terminalCommandsServesThePublishedList() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine()), publishedContentClient()) }

        val response = client.get("/api/terminal-commands")
        val terminalCommands = json.decodeFromString<TerminalTextCommands>(response.bodyAsText())

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(PublishedTerminalCommandsFixture, terminalCommands)
        assertEquals(listOf("neofetch", "sudo"), terminalCommands.items.map { it.keyword })
    }

    @Test
    fun terminalCommandsReturnsServiceUnavailableWhenNothingIsPublished() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine())) }

        assertEquals(HttpStatusCode.ServiceUnavailable, client.get("/api/terminal-commands").status)
    }

    @Test
    fun corsAllowsTheProductionOrigin() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine()), publishedContentClient()) }

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

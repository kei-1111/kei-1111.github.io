package io.github.kei_1111.server.contract

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.configureApplication
import io.github.kei_1111.server.content.DefaultTerminalTextCommands
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.ContributionDay
import io.github.kei_1111.shared.model.GitHubIssue
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.MarkdownListItem
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.RepoLanguage
import io.github.kei_1111.shared.model.TerminalTextCommand
import io.github.kei_1111.shared.model.TerminalTextCommands
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.WorkTag
import io.github.kei_1111.shared.model.Works
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.server.testing.testApplication
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// この形状が変わると独立デプロイされた client/server 間で silent degradation が起きるため、
// フィクスチャで wire 形状を固定する。
private val json = Json

// Mirrors the client parsers' config; pins the shared-model compatibility guarantee, not their implementation.
private val forwardCompatibleJson = Json { ignoreUnknownKeys = true }

private val PROFILE_FIXTURE =
    """
    {
      "name": {
        "ja": "けい",
        "en": "Kei"
      },
      "handle": "kei-1111",
      "location": "Japan",
      "role": "Software Engineer",
      "iconUrl": "images/profile-icon.webp",
      "followers": 10,
      "following": 20,
      "repos": 30,
      "totalStars": 40,
      "pinnedRepos": [
        {
          "name": "kotlin-repo",
          "description": {
            "ja": "Kotlin リポジトリ",
            "en": "Kotlin repository"
          },
          "url": "https://example.com/kotlin-repo",
          "language": "Kotlin",
          "stars": 5
        },
        {
          "name": "swift-repo",
          "description": {
            "ja": "Swift リポジトリ",
            "en": "Swift repository"
          },
          "url": "https://example.com/swift-repo",
          "language": "Swift"
        },
        {
          "name": "typescript-repo",
          "description": {
            "ja": "TypeScript リポジトリ",
            "en": "TypeScript repository"
          },
          "url": "https://example.com/typescript-repo",
          "language": "TypeScript"
        },
        {
          "name": "other-repo",
          "description": {
            "ja": "オプションフィールド無しのリポジトリ",
            "en": "Repository without optional fields"
          },
          "url": "https://example.com/other-repo"
        }
      ],
      "languages": [
        {
          "language": "Kotlin",
          "share": 0.5,
          "color": "#A97BFF"
        },
        {
          "language": "Swift",
          "share": 0.25
        },
        {
          "language": "TypeScript",
          "share": 0.25
        }
      ],
      "links": [
        {
          "type": "GitHub",
          "name": "GitHub",
          "url": "https://github.com/kei-1111"
        },
        {
          "type": "X",
          "name": "X",
          "url": "https://x.com/kei-1111"
        },
        {
          "type": "Qiita",
          "name": "Qiita",
          "url": "https://qiita.com/kei-1111"
        },
        {
          "type": "Note",
          "name": "Note",
          "url": "https://note.com/kei-1111"
        }
      ]
    }
    """.trimIndent()

private val UNKNOWN_ENUM_FIXTURE =
    """
    {
      "name": {
        "ja": "けい",
        "en": "Kei"
      },
      "handle": "kei-1111",
      "location": "Japan",
      "role": "Software Engineer",
      "followers": 10,
      "following": 20,
      "repos": 30,
      "totalStars": 40,
      "pinnedRepos": [
        {
          "name": "rust-repo",
          "description": {
            "ja": "Rust リポジトリ",
            "en": "Rust repository"
          },
          "url": "https://example.com/rust-repo",
          "language": "Rust",
          "stars": 5
        }
      ],
      "languages": [
        {
          "language": "Kotlin",
          "share": 0.5
        },
        {
          "language": "Rust",
          "share": 0.5
        }
      ],
      "links": [
        {
          "type": "GitHub",
          "name": "GitHub",
          "url": "https://github.com/kei-1111"
        },
        {
          "type": "LinkedIn",
          "name": "LinkedIn",
          "url": "https://www.linkedin.com/in/kei-1111"
        }
      ]
    }
    """.trimIndent()

private val BROKEN_LANGUAGE_FIXTURE =
    """
    {
      "name": {
        "ja": "けい",
        "en": "Kei"
      },
      "handle": "kei-1111",
      "location": "Japan",
      "role": "Software Engineer",
      "followers": 10,
      "following": 20,
      "repos": 30,
      "totalStars": 40,
      "pinnedRepos": [],
      "languages": [
        {
          "language": 123,
          "share": 1.0
        }
      ],
      "links": []
    }
    """.trimIndent()

private val CONTRIBUTIONS_FIXTURE =
    """
    {
      "totalLastYear": 7,
      "days": [
        {
          "date": "2026-01-01",
          "count": 0,
          "level": 0
        },
        {
          "date": "2026-01-02",
          "count": 7,
          "level": 4
        }
      ]
    }
    """.trimIndent()

private val WORKS_FIXTURE =
    """
    {
      "items": [
      {
        "id": "sample-app",
        "name": "Sample App",
        "kind": "Sample App",
        "period": "2024–",
        "description": {
          "ja": "サンプルアプリ",
          "en": "Sample app"
        },
        "tags": [
          { "name": "Kotlin", "accent": true },
          { "name": "Compose" }
        ],
        "roles": [
          { "ja": "設計", "en": "Design" }
        ],
        "iconUrl": "https://example.com/icon.webp",
        "screenshots": ["https://example.com/1.webp", "https://example.com/2.webp"],
        "storeUrl": "https://example.com/store"
      },
      {
        "id": "sample-site",
        "name": "Sample Site",
        "kind": "Website",
        "period": "2025–",
        "description": {
          "ja": "サンプルサイト",
          "en": "Sample site"
        },
        "sourceUrl": "https://example.com/source"
      }
      ]
    }
    """.trimIndent()

private val TERMINAL_TEXT_COMMANDS_FIXTURE =
    """
    {
      "items": [
        {
          "keyword": "neofetch",
          "description": "show portfolio system info",
          "lines": [
            " _  __  _____   ___    kei@kei-1111.github.io",
            "| |/ / | ____| |_ _|   ----------------------",
            "| ' /  |  _|    | |    OS: Android Studio New UI (Web Edition)",
            "| . \\  | |___   | |    Host: GitHub Pages",
            "|_|\\_\\ |_____| |___|   Kernel: Kotlin/Wasm + Compose Multiplatform",
            "                       Shell: zsh (portfolio flavored)",
            "                       Theme: Islands Dark / Islands Light",
            "                       Server: Ktor on Cloud Run"
          ]
        },
        {
          "keyword": "sudo",
          "description": "run a command as another user",
          "lines": [
            "kei is not in the sudoers file. This incident will be reported."
          ]
        }
      ]
    }
    """.trimIndent()

private val README_FIXTURE =
    """
    {
      "ja": [
        {
          "type": "heading",
          "level": 1,
          "inlines": [
            { "type": "plain_text", "text": "README" }
          ]
        },
        {
          "type": "paragraph",
          "inlines": [
            { "type": "plain_text", "text": "Run " },
            { "type": "inline_code", "text": "./gradlew test" },
            { "type": "link", "text": "Repository", "url": "https://example.com" }
          ]
        },
        {
          "type": "bullet_list",
          "items": [
            {
              "inlines": [
                { "type": "plain_text", "text": "First" }
              ]
            }
          ]
        }
      ],
      "en": [
        {
          "type": "paragraph",
          "inlines": [
            { "type": "plain_text", "text": "English" }
          ]
        }
      ]
    }
    """.trimIndent()

private val ISSUES_FIXTURE =
    """
    {
      "totalCount": 2,
      "issues": [
        {
          "number": 106,
          "title": "Add a TODO tool window showing the repository's real open Issues",
          "url": "https://github.com/kei-1111/kei-1111.github.io/issues/106",
          "type": "Feature"
        },
        {
          "number": 24,
          "title": "作品ページの追加（作品 API + クライアント UI）",
          "url": "https://github.com/kei-1111/kei-1111.github.io/issues/24"
        }
      ]
    }
    """.trimIndent()

private const val PROFILE_ROUTE_RESPONSE = """
{"data":{"user":{
  "followers":{"totalCount":101},
  "following":{"totalCount":102},
  "repositories":{"totalCount":103,"nodes":[
    {"languages":{"edges":[
      {"size":900,"node":{"name":"TypeScript","color":"#3178C6"}},
      {"size":100,"node":{"name":"Kotlin","color":"#A97BFF"}}
    ]}}
  ]},
  "starredRepositories":{"totalCount":104},
  "pinnedItems":{"nodes":[
    {
      "name":"kei-1111.github.io",
      "description":"GitHub description that must be overridden",
      "url":"https://github.com/kei-1111/kei-1111.github.io",
      "stargazerCount":0,
      "primaryLanguage":{"name":"Kotlin"}
    },
    {
      "name":"wire-shape-repo",
      "description":"Wire shape repository",
      "url":"https://github.com/kei-1111/wire-shape-repo",
      "stargazerCount":2,
      "primaryLanguage":null
    }
  ]}
}}}
"""

private const val CONTRIBUTIONS_ROUTE_RESPONSE = """
{"data":{"user":{"contributionsCollection":{"contributionCalendar":{
  "totalContributions":105,
  "weeks":[{"contributionDays":[
    {"date":"2026-08-05","contributionCount":106,"contributionLevel":"THIRD_QUARTILE"}
  ]}]
}}}}}
"""

private const val ISSUES_ROUTE_RESPONSE = """
{"data":{"repository":{"issues":{
  "totalCount":2,
  "nodes":[
    {"number":107,"title":"[Feature]: Pinned type","url":"https://example.com/issues/107"},
    {"number":108,"title":"Pinned nullable default","url":"https://example.com/issues/108"}
  ]
}}}}
"""

private fun routeEngine(body: String) = MockEngine {
    respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
}

private fun SerialDescriptor.fieldNames(): List<String> =
    (0 until elementsCount).map(::getElementName)

class SharedModelContractTest {

    @Test
    fun modelFieldNamesArePinned() {
        assertEquals(
            listOf(
                "name",
                "handle",
                "location",
                "role",
                "iconUrl",
                "followers",
                "following",
                "repos",
                "totalStars",
                "pinnedRepos",
                "languages",
                "links",
            ),
            GitHubProfile.serializer().descriptor.fieldNames(),
        )
        assertEquals(listOf("ja", "en"), LocalizedText.serializer().descriptor.fieldNames())
        assertEquals(listOf("ja", "en"), Readme.serializer().descriptor.fieldNames())
        assertEquals(listOf("inlines"), MarkdownListItem.serializer().descriptor.fieldNames())
        assertEquals(listOf("level", "inlines"), MarkdownBlock.Heading.serializer().descriptor.fieldNames())
        assertEquals(listOf("inlines"), MarkdownBlock.Paragraph.serializer().descriptor.fieldNames())
        assertEquals(listOf("items"), MarkdownBlock.BulletList.serializer().descriptor.fieldNames())
        assertEquals(listOf("text"), MarkdownInline.PlainText.serializer().descriptor.fieldNames())
        assertEquals(listOf("text"), MarkdownInline.InlineCode.serializer().descriptor.fieldNames())
        assertEquals(listOf("text", "url"), MarkdownInline.Link.serializer().descriptor.fieldNames())
        assertEquals(
            listOf("name", "description", "url", "language", "stars"),
            PinnedRepo.serializer().descriptor.fieldNames(),
        )
        assertEquals(listOf("language", "share", "color"), LanguageShare.serializer().descriptor.fieldNames())
        assertEquals(listOf("type", "name", "url"), LinkService.serializer().descriptor.fieldNames())
        assertEquals(listOf("totalLastYear", "days"), ContributionCalendar.serializer().descriptor.fieldNames())
        assertEquals(listOf("date", "count", "level"), ContributionDay.serializer().descriptor.fieldNames())
        assertEquals(listOf("totalCount", "issues"), GitHubIssues.serializer().descriptor.fieldNames())
        assertEquals(listOf("number", "title", "url", "type"), GitHubIssue.serializer().descriptor.fieldNames())
        assertEquals(
            listOf("keyword", "description", "lines"),
            TerminalTextCommand.serializer().descriptor.fieldNames(),
        )
        assertEquals(listOf("items"), TerminalTextCommands.serializer().descriptor.fieldNames())
    }

    @Test
    fun productionProfileRouteEmitsThePinnedWireShape() = testApplication {
        application { configureApplication(GitHubClient("test-token", routeEngine(PROFILE_ROUTE_RESPONSE))) }

        val response = client.get("/api/profile")
        val profile = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val pinnedRepos = profile.getValue("pinnedRepos").jsonArray

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(GitHubProfile.serializer().descriptor.fieldNames().toSet(), profile.keys)
        assertEquals(JsonPrimitive(101), profile["followers"])
        assertEquals(setOf("ja", "en"), profile.getValue("name").jsonObject.keys)
        assertEquals(PinnedRepo.serializer().descriptor.fieldNames().toSet(), pinnedRepos[0].jsonObject.keys)
        assertEquals(JsonPrimitive("kei-1111.github.io"), pinnedRepos[0].jsonObject["name"])
        assertEquals(JsonPrimitive("Kotlin"), pinnedRepos[0].jsonObject["language"])
        assertEquals(JsonNull, pinnedRepos[0].jsonObject["stars"])
        assertEquals(PinnedRepo.serializer().descriptor.fieldNames().toSet(), pinnedRepos[1].jsonObject.keys)
        assertEquals(JsonPrimitive("wire-shape-repo"), pinnedRepos[1].jsonObject["name"])
        assertEquals(JsonNull, pinnedRepos[1].jsonObject["language"])
        assertEquals(JsonPrimitive(2), pinnedRepos[1].jsonObject["stars"])
        assertEquals(setOf("language", "share", "color"), profile.getValue("languages").jsonArray[0].jsonObject.keys)
        assertEquals(setOf("type", "name", "url"), profile.getValue("links").jsonArray[0].jsonObject.keys)
    }

    @Test
    fun productionContributionsRouteEmitsThePinnedWireShape() = testApplication {
        application { configureApplication(GitHubClient("test-token", routeEngine(CONTRIBUTIONS_ROUTE_RESPONSE))) }

        val response = client.get("/api/contributions")
        val calendar = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val days = calendar.getValue("days") as JsonArray

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(setOf("totalLastYear", "days"), calendar.keys)
        assertEquals(JsonPrimitive(105), calendar["totalLastYear"])
        assertEquals(setOf("date", "count", "level"), days.single().jsonObject.keys)
        assertEquals(JsonPrimitive(106), days.single().jsonObject["count"])
    }

    @Test
    fun productionIssuesRouteEmitsThePinnedWireShape() = testApplication {
        application { configureApplication(GitHubClient("test-token", routeEngine(ISSUES_ROUTE_RESPONSE))) }

        val response = client.get("/api/issues")
        val issues = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val items = issues.getValue("issues").jsonArray

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(setOf("totalCount", "issues"), issues.keys)
        assertEquals(setOf("number", "title", "url", "type"), items[0].jsonObject.keys)
        assertEquals(JsonPrimitive("Feature"), items[0].jsonObject["type"])
        assertEquals(setOf("number", "title", "url", "type"), items[1].jsonObject.keys)
        assertEquals(JsonNull, items[1].jsonObject["type"])
    }

    @Test
    fun productionTerminalCommandsRouteEmitsThePinnedWireShape() = testApplication {
        application { configureApplication(GitHubClient("test-token", routeEngine(PROFILE_ROUTE_RESPONSE))) }

        val response = client.get("/api/terminal-commands")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            Json.parseToJsonElement(TERMINAL_TEXT_COMMANDS_FIXTURE),
            Json.parseToJsonElement(body),
        )
        assertEquals(
            DefaultTerminalTextCommands,
            json.decodeFromString<TerminalTextCommands>(body),
        )
    }

    @Test
    fun productionReadmeRouteEmitsThePinnedWireShape() = testApplication {
        application { configureApplication(GitHubClient("test-token", routeEngine("{}"))) }

        val response = client.get("/api/readme")
        val readme = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val ja = readme.getValue("ja").jsonArray
        val heading = ja.first().jsonObject
        val bulletLists = ja.filter { it.jsonObject["type"] == JsonPrimitive("bullet_list") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(setOf("ja", "en"), readme.keys)
        assertEquals(setOf("type", "level", "inlines"), heading.keys)
        assertEquals(JsonPrimitive("heading"), heading["type"])
        assertEquals(2, bulletLists.size)
        bulletLists.forEach { bulletList ->
            val block = bulletList.jsonObject
            assertEquals(setOf("type", "items"), block.keys)
            assertEquals(JsonPrimitive("bullet_list"), block["type"])
            block.getValue("items").jsonArray.forEach { item ->
                assertEquals(setOf("inlines"), item.jsonObject.keys)
            }
        }
    }

    @Test
    fun issuesWireShapeIsPinned() {
        val expected = GitHubIssues(
            totalCount = 2,
            issues = persistentListOf(
                GitHubIssue(
                    number = 106,
                    title = "Add a TODO tool window showing the repository's real open Issues",
                    url = "https://github.com/kei-1111/kei-1111.github.io/issues/106",
                    type = "Feature",
                ),
                GitHubIssue(
                    number = 24,
                    title = "作品ページの追加（作品 API + クライアント UI）",
                    url = "https://github.com/kei-1111/kei-1111.github.io/issues/24",
                ),
            ),
        )

        assertEquals(expected, json.decodeFromString<GitHubIssues>(ISSUES_FIXTURE))
        assertEquals(Json.parseToJsonElement(ISSUES_FIXTURE), json.encodeToJsonElement(expected))
    }

    @Test
    fun issuesWithUnknownTopLevelFieldDecodesForForwardCompatibility() {
        val fixture = json.parseToJsonElement(ISSUES_FIXTURE) as JsonObject
        val extendedFixture = JsonObject(fixture + ("fieldAddedByNewerServer" to JsonPrimitive(1)))
        val encodedFixture = forwardCompatibleJson.encodeToString(JsonObject.serializer(), extendedFixture)

        assertEquals(
            json.decodeFromString<GitHubIssues>(ISSUES_FIXTURE),
            forwardCompatibleJson.decodeFromString<GitHubIssues>(encodedFixture),
        )
    }

    @Test
    fun profileWireShapeIsPinned() {
        val expected = GitHubProfile(
            name = LocalizedText(ja = "けい", en = "Kei"),
            handle = "kei-1111",
            location = "Japan",
            role = "Software Engineer",
            iconUrl = "images/profile-icon.webp",
            followers = 10,
            following = 20,
            repos = 30,
            totalStars = 40,
            pinnedRepos = persistentListOf(
                PinnedRepo(
                    name = "kotlin-repo",
                    description = LocalizedText(ja = "Kotlin リポジトリ", en = "Kotlin repository"),
                    url = "https://example.com/kotlin-repo",
                    language = RepoLanguage("Kotlin"),
                    stars = 5,
                ),
                PinnedRepo(
                    name = "swift-repo",
                    description = LocalizedText(ja = "Swift リポジトリ", en = "Swift repository"),
                    url = "https://example.com/swift-repo",
                    language = RepoLanguage("Swift"),
                ),
                PinnedRepo(
                    name = "typescript-repo",
                    description = LocalizedText(ja = "TypeScript リポジトリ", en = "TypeScript repository"),
                    url = "https://example.com/typescript-repo",
                    language = RepoLanguage("TypeScript"),
                ),
                PinnedRepo(
                    name = "other-repo",
                    description = LocalizedText(
                        ja = "オプションフィールド無しのリポジトリ",
                        en = "Repository without optional fields",
                    ),
                    url = "https://example.com/other-repo",
                ),
            ),
            languages = persistentListOf(
                LanguageShare(language = RepoLanguage("Kotlin"), share = 0.5f, color = "#A97BFF"),
                LanguageShare(language = RepoLanguage("Swift"), share = 0.25f),
                LanguageShare(language = RepoLanguage("TypeScript"), share = 0.25f),
            ),
            links = persistentListOf(
                LinkService(
                    type = LinkServiceType.GitHub,
                    name = "GitHub",
                    url = "https://github.com/kei-1111",
                ),
                LinkService(
                    type = LinkServiceType.X,
                    name = "X",
                    url = "https://x.com/kei-1111",
                ),
                LinkService(
                    type = LinkServiceType.Qiita,
                    name = "Qiita",
                    url = "https://qiita.com/kei-1111",
                ),
                LinkService(
                    type = LinkServiceType.Note,
                    name = "Note",
                    url = "https://note.com/kei-1111",
                ),
            ),
        )

        assertEquals(expected, json.decodeFromString<GitHubProfile>(PROFILE_FIXTURE))
        assertEquals(Json.parseToJsonElement(PROFILE_FIXTURE), json.encodeToJsonElement(expected))
    }

    @Test
    fun profileWithUnknownTopLevelFieldDecodesForForwardCompatibility() {
        val fixture = json.parseToJsonElement(PROFILE_FIXTURE) as JsonObject
        val extendedFixture = JsonObject(fixture + ("fieldAddedByNewerServer" to JsonPrimitive(1)))
        val encodedFixture = forwardCompatibleJson.encodeToString(JsonObject.serializer(), extendedFixture)

        assertEquals(
            json.decodeFromString<GitHubProfile>(PROFILE_FIXTURE),
            forwardCompatibleJson.decodeFromString<GitHubProfile>(encodedFixture),
        )
    }

    @Test
    fun arbitraryLanguageNamesDecodeWhileUnknownLinkServiceDrops() {
        val expected = GitHubProfile(
            name = LocalizedText(ja = "けい", en = "Kei"),
            handle = "kei-1111",
            location = "Japan",
            role = "Software Engineer",
            followers = 10,
            following = 20,
            repos = 30,
            totalStars = 40,
            pinnedRepos = persistentListOf(
                PinnedRepo(
                    name = "rust-repo",
                    description = LocalizedText(ja = "Rust リポジトリ", en = "Rust repository"),
                    url = "https://example.com/rust-repo",
                    language = RepoLanguage("Rust"),
                    stars = 5,
                ),
            ),
            languages = persistentListOf(
                LanguageShare(language = RepoLanguage("Kotlin"), share = 0.5f),
                LanguageShare(language = RepoLanguage("Rust"), share = 0.5f),
            ),
            links = persistentListOf(
                LinkService(
                    type = LinkServiceType.GitHub,
                    name = "GitHub",
                    url = "https://github.com/kei-1111",
                ),
            ),
        )

        assertEquals(expected, json.decodeFromString<GitHubProfile>(UNKNOWN_ENUM_FIXTURE))
    }

    @Test
    fun structurallyBrokenLanguageValuesStillFailDecode() {
        assertFailsWith<SerializationException> {
            json.decodeFromString<GitHubProfile>(BROKEN_LANGUAGE_FIXTURE)
        }
    }

    @Test
    fun contributionsWireShapeIsPinned() {
        val expected = ContributionCalendar(
            totalLastYear = 7,
            days = persistentListOf(
                ContributionDay(date = "2026-01-01", count = 0, level = 0),
                ContributionDay(date = "2026-01-02", count = 7, level = 4),
            ),
        )

        assertEquals(expected, json.decodeFromString<ContributionCalendar>(CONTRIBUTIONS_FIXTURE))
        assertEquals(Json.parseToJsonElement(CONTRIBUTIONS_FIXTURE), json.encodeToJsonElement(expected))
    }

    @Test
    fun contributionsWithUnknownTopLevelFieldDecodesForForwardCompatibility() {
        val fixture = json.parseToJsonElement(CONTRIBUTIONS_FIXTURE) as JsonObject
        val extendedFixture = JsonObject(fixture + ("fieldAddedByNewerServer" to JsonPrimitive(1)))
        val encodedFixture = forwardCompatibleJson.encodeToString(JsonObject.serializer(), extendedFixture)

        assertEquals(
            json.decodeFromString<ContributionCalendar>(CONTRIBUTIONS_FIXTURE),
            forwardCompatibleJson.decodeFromString<ContributionCalendar>(encodedFixture),
        )
    }

    @Test
    fun readmeWireShapeIsPinned() {
        val expected = Readme(
            ja = persistentListOf(
                MarkdownBlock.Heading(
                    level = 1,
                    inlines = persistentListOf(MarkdownInline.PlainText("README")),
                ),
                MarkdownBlock.Paragraph(
                    inlines = persistentListOf(
                        MarkdownInline.PlainText("Run "),
                        MarkdownInline.InlineCode("./gradlew test"),
                        MarkdownInline.Link(text = "Repository", url = "https://example.com"),
                    ),
                ),
                MarkdownBlock.BulletList(
                    items = persistentListOf(
                        MarkdownListItem(
                            inlines = persistentListOf(MarkdownInline.PlainText("First")),
                        ),
                    ),
                ),
            ),
            en = persistentListOf(
                MarkdownBlock.Paragraph(
                    inlines = persistentListOf(MarkdownInline.PlainText("English")),
                ),
            ),
        )

        assertEquals(expected, json.decodeFromString<Readme>(README_FIXTURE))
        assertEquals(Json.parseToJsonElement(README_FIXTURE), json.encodeToJsonElement(expected))
    }

    @Test
    fun readmeWithUnknownFieldOnABlockDecodesForForwardCompatibility() {
        val fixture = json.parseToJsonElement(README_FIXTURE) as JsonObject
        val blocks = fixture.getValue("ja") as JsonArray
        val extendedFirst = JsonObject((blocks[0] as JsonObject) + ("fieldAddedByNewerServer" to JsonPrimitive(1)))
        val extendedBlocks = JsonArray(listOf(extendedFirst) + blocks.drop(1))
        val extendedFixture = JsonObject(fixture + ("ja" to extendedBlocks))
        val encodedFixture = forwardCompatibleJson.encodeToString(JsonObject.serializer(), extendedFixture)

        assertEquals(
            json.decodeFromString<Readme>(README_FIXTURE),
            forwardCompatibleJson.decodeFromString<Readme>(encodedFixture),
        )
    }

    @Test
    fun worksWireShapeIsPinned() {
        val expected = Works(
            items = persistentListOf(
                Work(
                    id = "sample-app",
                    name = "Sample App",
                    kind = "Sample App",
                    period = "2024–",
                    description = LocalizedText(ja = "サンプルアプリ", en = "Sample app"),
                    tags = persistentListOf(
                        WorkTag(name = "Kotlin", accent = true),
                        WorkTag(name = "Compose"),
                    ),
                    roles = persistentListOf(LocalizedText(ja = "設計", en = "Design")),
                    iconUrl = "https://example.com/icon.webp",
                    screenshots = persistentListOf("https://example.com/1.webp", "https://example.com/2.webp"),
                    storeUrl = "https://example.com/store",
                    sourceUrl = null,
                ),
                Work(
                    id = "sample-site",
                    name = "Sample Site",
                    kind = "Website",
                    period = "2025–",
                    description = LocalizedText(ja = "サンプルサイト", en = "Sample site"),
                    tags = persistentListOf(),
                    roles = persistentListOf(),
                    iconUrl = null,
                    screenshots = persistentListOf(),
                    storeUrl = null,
                    sourceUrl = "https://example.com/source",
                ),
            ),
        )

        assertEquals(expected, json.decodeFromString<Works>(WORKS_FIXTURE))
        assertEquals(Json.parseToJsonElement(WORKS_FIXTURE), json.encodeToJsonElement(expected))
    }

    @Test
    fun worksWithUnknownFieldOnAnElementDecodesForForwardCompatibility() {
        val fixture = json.parseToJsonElement(WORKS_FIXTURE) as JsonObject
        val items = fixture.getValue("items") as JsonArray
        val extendedFirst = JsonObject((items[0] as JsonObject) + ("fieldAddedByNewerServer" to JsonPrimitive(1)))
        val extendedItems = JsonArray(listOf(extendedFirst) + items.drop(1))
        val extendedFixture = JsonObject(fixture + ("items" to extendedItems))
        val encodedFixture = forwardCompatibleJson.encodeToString(JsonObject.serializer(), extendedFixture)

        assertEquals(
            json.decodeFromString<Works>(WORKS_FIXTURE),
            forwardCompatibleJson.decodeFromString<Works>(encodedFixture),
        )
    }

    @Test
    fun terminalCommandsWithUnknownFieldOnAnElementDecodesForForwardCompatibility() {
        val fixture = json.parseToJsonElement(TERMINAL_TEXT_COMMANDS_FIXTURE) as JsonObject
        val items = fixture.getValue("items") as JsonArray
        val extendedFirst = JsonObject((items[0] as JsonObject) + ("fieldAddedByNewerServer" to JsonPrimitive(1)))
        val extendedItems = JsonArray(listOf(extendedFirst) + items.drop(1))
        val extendedFixture = JsonObject(fixture + ("items" to extendedItems))
        val encodedFixture = forwardCompatibleJson.encodeToString(JsonObject.serializer(), extendedFixture)

        assertEquals(
            json.decodeFromString<TerminalTextCommands>(TERMINAL_TEXT_COMMANDS_FIXTURE),
            forwardCompatibleJson.decodeFromString<TerminalTextCommands>(encodedFixture),
        )
    }

    @Test
    fun repoLanguageWireFormIsAPlainString() {
        val language = RepoLanguage("X")

        val encoded = json.encodeToString(RepoLanguage.serializer(), language)

        assertEquals("\"X\"", encoded)
        assertEquals(language, json.decodeFromString<RepoLanguage>(encoded))
    }

    @Test
    fun linkServiceTypeSerialNamesArePinned() {
        assertEquals(
            listOf("GitHub", "X", "Qiita", "Note"),
            LinkServiceType.entries.map { json.encodeToString(LinkServiceType.serializer(), it).trim('"') },
        )
    }
}

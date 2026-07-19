package io.github.kei_1111.server.contract

import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.ContributionDay
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

// この形状が変わると独立デプロイされた client/server 間で silent degradation が起きるため、
// フィクスチャで wire 形状を固定する。
private val json = Json

private val PROFILE_FIXTURE =
    """
    {
      "name": "Kei",
      "handle": "kei-1111",
      "location": "Japan",
      "role": "Software Engineer",
      "followers": 10,
      "following": 20,
      "repos": 30,
      "totalStars": 40,
      "pinnedRepos": [
        {
          "name": "kotlin-repo",
          "description": "Kotlin repository",
          "url": "https://example.com/kotlin-repo",
          "language": "Kotlin",
          "stars": 5
        },
        {
          "name": "swift-repo",
          "description": "Swift repository",
          "url": "https://example.com/swift-repo",
          "language": "Swift"
        },
        {
          "name": "shell-repo",
          "description": "Shell repository",
          "url": "https://example.com/shell-repo",
          "language": "Shell"
        },
        {
          "name": "other-repo",
          "description": "Repository without optional fields",
          "url": "https://example.com/other-repo"
        }
      ],
      "languages": [
        {
          "language": "Kotlin",
          "share": 0.5
        },
        {
          "language": "Swift",
          "share": 0.25
        },
        {
          "language": "Shell",
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

class SharedModelContractTest {

    @Test
    fun profileWireShapeIsPinned() {
        val expected = GitHubProfile(
            name = "Kei",
            handle = "kei-1111",
            location = "Japan",
            role = "Software Engineer",
            followers = 10,
            following = 20,
            repos = 30,
            totalStars = 40,
            pinnedRepos = persistentListOf(
                PinnedRepo(
                    name = "kotlin-repo",
                    description = "Kotlin repository",
                    url = "https://example.com/kotlin-repo",
                    language = RepoLanguage.Kotlin,
                    stars = 5,
                ),
                PinnedRepo(
                    name = "swift-repo",
                    description = "Swift repository",
                    url = "https://example.com/swift-repo",
                    language = RepoLanguage.Swift,
                ),
                PinnedRepo(
                    name = "shell-repo",
                    description = "Shell repository",
                    url = "https://example.com/shell-repo",
                    language = RepoLanguage.Shell,
                ),
                PinnedRepo(
                    name = "other-repo",
                    description = "Repository without optional fields",
                    url = "https://example.com/other-repo",
                ),
            ),
            languages = persistentListOf(
                LanguageShare(language = RepoLanguage.Kotlin, share = 0.5f),
                LanguageShare(language = RepoLanguage.Swift, share = 0.25f),
                LanguageShare(language = RepoLanguage.Shell, share = 0.25f),
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
    fun repoLanguageSerialNamesArePinned() {
        assertEquals(
            listOf("Kotlin", "Swift", "Shell"),
            RepoLanguage.entries.map { json.encodeToString(RepoLanguage.serializer(), it).trim('"') },
        )
    }

    @Test
    fun linkServiceTypeSerialNamesArePinned() {
        assertEquals(
            listOf("GitHub", "X", "Qiita", "Note"),
            LinkServiceType.entries.map { json.encodeToString(LinkServiceType.serializer(), it).trim('"') },
        )
    }
}

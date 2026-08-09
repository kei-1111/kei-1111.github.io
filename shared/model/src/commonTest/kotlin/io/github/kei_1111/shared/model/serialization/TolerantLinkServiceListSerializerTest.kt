package io.github.kei_1111.shared.model.serialization

import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private val json = Json

private val ARBITRARY_LANGUAGE_FIXTURE =
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
        },
        {
          "name": "kotlin-repo",
          "description": {
            "ja": "Kotlin リポジトリ",
            "en": "Kotlin repository"
          },
          "url": "https://example.com/kotlin-repo",
          "language": "Kotlin",
          "stars": 3
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

private val BROKEN_LANGUAGE_FIXTURE = ARBITRARY_LANGUAGE_FIXTURE.replace(
    "\"language\": \"Rust\"",
    "\"language\": 123",
)

class TolerantLinkServiceListSerializerTest {

    @Test
    fun arbitraryRepoLanguageInPinnedRepoIsKept() {
        val decoded = json.decodeFromString<GitHubProfile>(ARBITRARY_LANGUAGE_FIXTURE)

        assertEquals(
            persistentListOf(
                PinnedRepo(
                    name = "rust-repo",
                    description = LocalizedText(ja = "Rust リポジトリ", en = "Rust repository"),
                    url = "https://example.com/rust-repo",
                    language = RepoLanguage("Rust"),
                    stars = 5,
                ),
                PinnedRepo(
                    name = "kotlin-repo",
                    description = LocalizedText(ja = "Kotlin リポジトリ", en = "Kotlin repository"),
                    url = "https://example.com/kotlin-repo",
                    language = RepoLanguage("Kotlin"),
                    stars = 3,
                ),
            ),
            decoded.pinnedRepos,
        )
    }

    @Test
    fun arbitraryRepoLanguageInLanguageSharesIsKept() {
        val decoded = json.decodeFromString<GitHubProfile>(ARBITRARY_LANGUAGE_FIXTURE)

        assertEquals(
            persistentListOf(
                LanguageShare(language = RepoLanguage("Kotlin"), share = 0.5f),
                LanguageShare(language = RepoLanguage("Rust"), share = 0.5f),
            ),
            decoded.languages,
        )
    }

    @Test
    fun structurallyBrokenRepoLanguageFailsDecode() {
        assertFailsWith<SerializationException> {
            json.decodeFromString<GitHubProfile>(BROKEN_LANGUAGE_FIXTURE)
        }
    }

    @Test
    fun unknownLinkServiceTypeDropsElementAndKeepsRest() {
        val decoded = json.decodeFromString<GitHubProfile>(ARBITRARY_LANGUAGE_FIXTURE)

        assertEquals(
            persistentListOf(
                LinkService(
                    type = LinkServiceType.GitHub,
                    name = "GitHub",
                    url = "https://github.com/kei-1111",
                ),
            ),
            decoded.links,
        )
    }
}

package io.github.kei_1111.shared.model.serialization

import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

// サーバー側 SharedModelContractTest が wire 契約を固定するのに対し、こちらは
// 本番で契約を消費する wasmJs ターゲット上でも劣化デコードが実行されることを保証する。
private val json = Json

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

class TolerantEnumListSerializersTest {

    @Test
    fun unknownRepoLanguageInPinnedRepoStripsFieldAndKeepsElement() {
        val decoded = json.decodeFromString<GitHubProfile>(UNKNOWN_ENUM_FIXTURE)

        assertEquals(
            persistentListOf(
                PinnedRepo(
                    name = "rust-repo",
                    description = LocalizedText(ja = "Rust リポジトリ", en = "Rust repository"),
                    url = "https://example.com/rust-repo",
                    language = null,
                    stars = 5,
                ),
                PinnedRepo(
                    name = "kotlin-repo",
                    description = LocalizedText(ja = "Kotlin リポジトリ", en = "Kotlin repository"),
                    url = "https://example.com/kotlin-repo",
                    language = RepoLanguage.Kotlin,
                    stars = 3,
                ),
            ),
            decoded.pinnedRepos,
        )
    }

    @Test
    fun unknownRepoLanguageInLanguageSharesDropsElementAndKeepsRest() {
        val decoded = json.decodeFromString<GitHubProfile>(UNKNOWN_ENUM_FIXTURE)

        assertEquals(
            persistentListOf(LanguageShare(language = RepoLanguage.Kotlin, share = 0.5f)),
            decoded.languages,
        )
    }

    @Test
    fun unknownLinkServiceTypeDropsElementAndKeepsRest() {
        val decoded = json.decodeFromString<GitHubProfile>(UNKNOWN_ENUM_FIXTURE)

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

package io.github.kei_1111.shared.model

import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class GitHubProfileSerializationTest {

    @Test
    fun defaultsIsFallbackToFalseWhenTheFieldIsMissing() {
        val profile = Json.decodeFromString(GitHubProfile.serializer(), PROFILE_WITHOUT_FALLBACK_FLAG)

        assertFalse(profile.isFallback)
    }

    @Test
    fun preservesTrueFallbackFlagThroughRoundTrip() {
        val expected = profile(isFallback = true)

        val encoded = Json.encodeToString(GitHubProfile.serializer(), expected)
        val decoded = Json.decodeFromString(GitHubProfile.serializer(), encoded)

        assertEquals(expected, decoded)
    }
}

private val PROFILE_WITHOUT_FALLBACK_FLAG =
    """
    {
      "name": { "ja": "テスト", "en": "Test" },
      "handle": "test",
      "location": "Tokyo",
      "role": "Developer",
      "followers": 1,
      "following": 2,
      "repos": 3,
      "totalStars": 4,
      "pinnedRepos": [],
      "languages": [],
      "links": []
    }
    """.trimIndent()

private fun profile(isFallback: Boolean) = GitHubProfile(
    name = LocalizedText(ja = "テスト", en = "Test"),
    handle = "test",
    location = "Tokyo",
    role = "Developer",
    followers = 1,
    following = 2,
    repos = 3,
    totalStars = 4,
    pinnedRepos = persistentListOf(),
    languages = persistentListOf(),
    links = persistentListOf(),
    isFallback = isFallback,
)

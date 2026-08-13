package io.github.kei_1111.shared.model

import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ProfileSerializationTest {

    @Test
    fun decodesAbsentStatisticsAsNull() {
        val profile = Json.decodeFromString(Profile.serializer(), PROFILE_WITHOUT_STATISTICS)

        assertNull(profile.followers)
        assertNull(profile.following)
        assertNull(profile.repos)
        assertNull(profile.totalStars)
    }

    @Test
    fun omitsAbsentStatisticsFromTheEncodedForm() {
        val encoded = Json.encodeToString(Profile.serializer(), profile(followers = null))

        assertFalse(encoded.contains("followers"))
    }

    @Test
    fun encodesPresentStatistics() {
        val encoded = Json.encodeToString(Profile.serializer(), profile(followers = 7))

        assertContains(encoded, "\"followers\":7")
    }

    @Test
    fun roundTripsAProfileWhoseStatisticsAreAbsent() {
        val expected = profile(followers = null)

        val decoded = Json.decodeFromString(Profile.serializer(), Json.encodeToString(Profile.serializer(), expected))

        assertEquals(expected, decoded)
    }
}

private val PROFILE_WITHOUT_STATISTICS =
    """
    {
      "name": { "ja": "テスト", "en": "Test" },
      "handle": "test",
      "location": "Tokyo",
      "role": "Developer",
      "pinnedRepos": [],
      "languages": [],
      "links": []
    }
    """.trimIndent()

private fun profile(followers: Int?) = Profile(
    name = LocalizedText(ja = "テスト", en = "Test"),
    handle = "test",
    location = "Tokyo",
    role = "Developer",
    followers = followers,
    following = followers,
    repos = followers,
    totalStars = followers,
    pinnedRepos = persistentListOf(),
    languages = persistentListOf(),
    links = persistentListOf(),
)

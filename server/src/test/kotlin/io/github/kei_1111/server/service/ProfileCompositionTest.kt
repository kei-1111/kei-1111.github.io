package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.FakePublishedContentClient
import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.PROFILE_LOGIN
import io.github.kei_1111.server.client.PublishedPinnedRepo
import io.github.kei_1111.server.client.PublishedProfile
import io.github.kei_1111.server.client.PublishedResult
import io.github.kei_1111.shared.model.LocalizedText
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileCompositionTest {

    @Test
    fun servesNoProfileWhenNothingIsPublished() = runTest {
        val service = ProfileService(successfulGitHubClient(), FakePublishedContentClient())

        assertNull(service.getProfile())
    }

    @Test
    fun servesNoProfileWhenThePublishedFetchFails() = runTest {
        val service = ProfileService(successfulGitHubClient(), FakePublishedContentClient(profile = null))

        assertNull(service.getProfile())
    }

    @Test
    fun composesThePublishedProfileWithTheGitHubStatistics() = runTest {
        val service = profileService(
            PublishedProfile(displayName = "けい", displayNameEn = "Kei", role = "Android Engineer"),
        )

        val profile = requireNotNull(service.getProfile())

        assertEquals(LocalizedText(ja = "けい", en = "Kei"), profile.name)
        assertEquals("Android Engineer", profile.role)
        assertEquals(1, profile.followers)
        assertEquals(2, profile.following)
        assertEquals(3, profile.repos)
        assertEquals(4, profile.totalStars)
    }

    @Test
    fun leavesTheStatisticsAbsentWhenGitHubIsUnreachable() = runTest {
        val service = ProfileService(
            failingGitHubClient(),
            FakePublishedContentClient(profile = PublishedResult.Found(PublishedProfile(displayName = "けい"))),
        )

        val profile = requireNotNull(service.getProfile())

        assertNull(profile.followers)
        assertNull(profile.following)
        assertNull(profile.repos)
        assertNull(profile.totalStars)
        assertTrue(profile.pinnedRepos.isEmpty())
        assertTrue(profile.languages.isEmpty())
    }

    @Test
    fun takesTheHandleFromTheLoginTheServerQueries() = runTest {
        val service = profileService(PublishedProfile(displayName = "けい"))

        assertEquals(PROFILE_LOGIN, requireNotNull(service.getProfile()).handle)
    }

    @Test
    fun describesPinnedRepositoriesFromThePublishedProfile() = runTest {
        val service = profileService(
            PublishedProfile(
                displayName = "けい",
                pinnedRepos = listOf(
                    PublishedPinnedRepo(name = "withmo", descriptionJa = "説明", descriptionEn = "Description"),
                ),
            ),
        )

        val repo = requireNotNull(service.getProfile()).pinnedRepos.single()

        assertEquals("withmo", repo.name)
        assertEquals(LocalizedText(ja = "説明", en = "Description"), repo.description)
    }

    @Test
    fun hidesPinnedRepositoriesTheAdminMarkedInvisible() = runTest {
        val service = profileService(
            PublishedProfile(
                displayName = "けい",
                pinnedRepos = listOf(PublishedPinnedRepo(name = "withmo", visible = false)),
            ),
        )

        assertTrue(requireNotNull(service.getProfile()).pinnedRepos.isEmpty())
    }

    @Test
    fun servesTheAdminUploadedAvatar() = runTest {
        val service = profileService(
            PublishedProfile(displayName = "けい", avatarUrl = "https://admin.example/images/profile/1-avatar.png"),
        )

        assertEquals(
            "https://admin.example/images/profile/1-avatar.png",
            requireNotNull(service.getProfile()).iconUrl,
        )
    }

    @Test
    fun leavesTheIconAbsentWhenNoAvatarWasUploaded() = runTest {
        val service = profileService(PublishedProfile(displayName = "けい"))

        assertNull(requireNotNull(service.getProfile()).iconUrl)
    }

    @Test
    fun keepsTheGitHubDescriptionWhenThePublishedProfileLeavesItBlank() = runTest {
        val service = profileService(
            PublishedProfile(
                displayName = "けい",
                pinnedRepos = listOf(PublishedPinnedRepo(name = "withmo")),
            ),
        )

        val repo = requireNotNull(service.getProfile()).pinnedRepos.single()

        assertEquals(LocalizedText(ja = "A home screen app", en = "A home screen app"), repo.description)
    }
}

private fun profileService(published: PublishedProfile) = ProfileService(
    gitHubClient = successfulGitHubClient(),
    publishedContentClient = FakePublishedContentClient(profile = PublishedResult.Found(published)),
)

private fun failingGitHubClient() = GitHubClient(
    token = "test-token",
    engine = MockEngine { respondError(HttpStatusCode.InternalServerError) },
)

private fun successfulGitHubClient() = GitHubClient(
    token = "test-token",
    engine = MockEngine {
        respond(
            content = SUCCESS_PROFILE_RESPONSE,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    },
)

private val SUCCESS_PROFILE_RESPONSE =
    """
    {
      "data": {
        "user": {
          "followers": { "totalCount": 1 },
          "following": { "totalCount": 2 },
          "repositories": { "totalCount": 3, "nodes": [] },
          "starredRepositories": { "totalCount": 4 },
          "pinnedItems": {
            "nodes": [
              {
                "name": "withmo",
                "description": "A home screen app",
                "url": "https://github.com/kei-1111/withmo",
                "stargazerCount": 5,
                "primaryLanguage": { "name": "Kotlin" }
              }
            ]
          }
        }
      }
    }
    """.trimIndent()

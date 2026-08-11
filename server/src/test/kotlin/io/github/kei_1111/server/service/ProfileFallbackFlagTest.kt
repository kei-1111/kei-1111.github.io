package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.FakePublishedContentClient
import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.PublishedProfile
import io.github.kei_1111.server.client.PublishedResult
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileFallbackFlagTest {

    @Test
    fun marksTheProfileAsFallbackWhenStatsFetchFails() = runTest {
        val service = ProfileService(
            gitHubClient = failingGitHubClient(),
            publishedContentClient = FakePublishedContentClient(),
        )

        val profile = service.getProfile()

        assertTrue(profile.isFallback)
    }

    @Test
    fun doesNotMarkTheProfileAsFallbackWhenStatsFetchSucceeds() = runTest {
        val service = ProfileService(
            gitHubClient = successfulGitHubClient(),
            publishedContentClient = FakePublishedContentClient(),
        )

        val profile = service.getProfile()

        assertFalse(profile.isFallback)
    }

    @Test
    fun preservesTheFallbackFlagWhenPublishedContentIsOverlaid() = runTest {
        val service = ProfileService(
            gitHubClient = failingGitHubClient(),
            publishedContentClient = FakePublishedContentClient(
                profile = PublishedResult.Found(PublishedProfile(displayName = "公開名")),
            ),
        )

        val profile = service.getProfile()

        assertTrue(profile.isFallback)
    }
}

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
          "pinnedItems": { "nodes": [] }
        }
      }
    }
    """.trimIndent()

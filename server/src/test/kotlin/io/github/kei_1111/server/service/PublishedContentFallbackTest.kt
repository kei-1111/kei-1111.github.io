package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.client.PublishedProfile
import io.github.kei_1111.server.content.DefaultGitHubProfile
import io.github.kei_1111.server.content.DefaultWorks
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.Works
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private val publishedWorks = Works(
    items = persistentListOf(
        Work(
            id = "published-work",
            name = "Published Work",
            kind = "Web App",
            period = "2026–",
            description = LocalizedText(ja = "公開", en = "published"),
        ),
    ),
)

private class FakePublishedContentClient(
    private val works: Works? = null,
    private val profile: PublishedProfile? = null,
) : PublishedContentClient {
    override suspend fun fetchWorks(): Works? = works
    override suspend fun fetchProfile(): PublishedProfile? = profile
}

/** GitHub API を常に失敗させ、静的フォールバック + 公開コンテンツだけの挙動を観察する。 */
private fun failingGitHubClient() = GitHubClient(
    token = "t",
    engine = MockEngine { respondError(HttpStatusCode.InternalServerError) },
)

class PublishedContentFallbackTest {

    @Test
    fun servesPublishedWorksWhenTheClientReturnsThem() = runTest {
        val service = WorksService(publishedContentClient = FakePublishedContentClient(works = publishedWorks))

        assertEquals(publishedWorks, service.getWorks())
    }

    @Test
    fun fallsBackToBuiltInWorksWhenNothingIsPublished() = runTest {
        val service = WorksService(publishedContentClient = FakePublishedContentClient(works = null))

        assertEquals(DefaultWorks, service.getWorks())
    }

    @Test
    fun overlaysThePublishedProfileOntoTheGitHubDerivedResponse() = runTest {
        val service = ProfileService(
            gitHubClient = failingGitHubClient(),
            publishedContentClient = FakePublishedContentClient(
                profile = PublishedProfile(displayName = "公開名", role = "Android Engineer"),
            ),
        )

        val profile = service.getProfile()

        assertEquals(LocalizedText(ja = "公開名", en = "公開名"), profile.name)
        assertEquals("Android Engineer", profile.role)
        // 統計はフォールバック(ビルトイン)値のまま
        assertEquals(DefaultGitHubProfile.followers, profile.followers)
    }

    @Test
    fun keepsCurrentProfileBehaviorWhenNothingIsPublished() = runTest {
        val service = ProfileService(
            gitHubClient = failingGitHubClient(),
            publishedContentClient = FakePublishedContentClient(),
        )

        assertEquals(DefaultGitHubProfile, service.getProfile())
    }
}

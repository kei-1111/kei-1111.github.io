package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.FakePublishedContentClient
import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.PublishedPinnedRepo
import io.github.kei_1111.server.client.PublishedProfile
import io.github.kei_1111.server.client.PublishedResult
import io.github.kei_1111.server.content.DefaultGitHubProfile
import io.github.kei_1111.server.content.DefaultReadme
import io.github.kei_1111.server.content.DefaultTerminalTextCommands
import io.github.kei_1111.server.content.DefaultWorks
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.TerminalTextCommand
import io.github.kei_1111.shared.model.TerminalTextCommands
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

/** GitHub API を常に失敗させ、静的フォールバック + 公開コンテンツだけの挙動を観察する。 */
private fun failingGitHubClient() = GitHubClient(
    token = "t",
    engine = MockEngine { respondError(HttpStatusCode.InternalServerError) },
)

class PublishedContentFallbackTest {

    @Test
    fun servesPublishedWorksWhenTheClientReturnsThem() = runTest {
        val service = WorksService(
            publishedContentClient = FakePublishedContentClient(works = PublishedResult.Found(publishedWorks)),
        )

        assertEquals(publishedWorks, service.getWorks())
    }

    @Test
    fun fallsBackToBuiltInWorksWhenPublishedContentIsMissing() = runTest {
        val service = WorksService(publishedContentClient = FakePublishedContentClient())

        assertEquals(DefaultWorks, service.getWorks())
    }

    @Test
    fun fallsBackToBuiltInWorksWhenTheInitialFetchFails() = runTest {
        val service = WorksService(publishedContentClient = FakePublishedContentClient(works = null))

        assertEquals(DefaultWorks, service.getWorks())
    }

    @Test
    fun overlaysThePublishedProfileOntoTheGitHubDerivedResponse() = runTest {
        val service = ProfileService(
            gitHubClient = failingGitHubClient(),
            publishedContentClient = FakePublishedContentClient(
                profile = PublishedResult.Found(
                    PublishedProfile(displayName = "公開名", role = "Android Engineer"),
                ),
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

        assertEquals(DefaultGitHubProfile.copy(isFallback = true), service.getProfile())
    }

    @Test
    fun servesPublishedReadmeAndFallsBackToBuiltIn() = runTest {
        val published = Readme(
            ja = persistentListOf(MarkdownBlock.Paragraph(inlines = persistentListOf(MarkdownInline.PlainText("公開")))),
            en = persistentListOf(MarkdownBlock.Paragraph(inlines = persistentListOf(MarkdownInline.PlainText("published")))),
        )

        assertEquals(
            published,
            ReadmeService(FakePublishedContentClient(readme = PublishedResult.Found(published))).getReadme(),
        )
        assertEquals(
            DefaultReadme,
            ReadmeService(FakePublishedContentClient()).getReadme(),
        )
    }

    @Test
    fun servesPublishedTerminalCommandsAndFallsBackToBuiltIn() = runTest {
        val published = TerminalTextCommands(
            items = persistentListOf(TerminalTextCommand(keyword = "coffee", lines = persistentListOf("brewing"))),
        )

        assertEquals(
            published,
            TerminalCommandsService(
                FakePublishedContentClient(terminalCommands = PublishedResult.Found(published)),
            ).getTerminalCommands(),
        )
        assertEquals(
            DefaultTerminalTextCommands,
            TerminalCommandsService(FakePublishedContentClient()).getTerminalCommands(),
        )
    }

    @Test
    fun overlaysAvatarAndPinnedDescriptionOverrides() = runTest {
        val service = ProfileService(
            gitHubClient = failingGitHubClient(),
            publishedContentClient = FakePublishedContentClient(
                profile = PublishedResult.Found(
                    PublishedProfile(
                        displayName = "けい",
                        avatarUrl = "https://admin.example/images/profile/1-avatar.png",
                        pinnedRepos = listOf(
                            PublishedPinnedRepo(
                                name = DefaultGitHubProfile.pinnedRepos.first().name,
                                descriptionJa = "上書き説明",
                                descriptionEn = "Overridden description",
                            ),
                        ),
                    ),
                ),
            ),
        )

        val profile = service.getProfile()

        assertEquals("https://admin.example/images/profile/1-avatar.png", profile.iconUrl)
        assertEquals(
            LocalizedText(ja = "上書き説明", en = "Overridden description"),
            profile.pinnedRepos.first().description,
        )
    }
}

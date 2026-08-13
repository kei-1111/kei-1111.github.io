package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.FakePublishedContentClient
import io.github.kei_1111.server.client.PublishedResult
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
}

package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.FakePublishedContentClient
import io.github.kei_1111.server.client.PublishedReadmeFixture
import io.github.kei_1111.server.client.PublishedResult
import io.github.kei_1111.server.client.PublishedTerminalCommandsFixture
import io.github.kei_1111.server.client.PublishedWorksFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PublishedContentRequirementTest {

    @Test
    fun servesPublishedWorks() = runTest {
        val service = WorksService(FakePublishedContentClient(works = PublishedResult.Found(PublishedWorksFixture)))

        assertEquals(PublishedWorksFixture, service.getWorks())
    }

    @Test
    fun servesNoWorksWhenNothingIsPublished() = runTest {
        assertNull(WorksService(FakePublishedContentClient()).getWorks())
    }

    @Test
    fun servesNoWorksWhenTheFetchFails() = runTest {
        assertNull(WorksService(FakePublishedContentClient(works = null)).getWorks())
    }

    @Test
    fun servesPublishedReadme() = runTest {
        val service = ReadmeService(FakePublishedContentClient(readme = PublishedResult.Found(PublishedReadmeFixture)))

        assertEquals(PublishedReadmeFixture, service.getReadme())
    }

    @Test
    fun servesNoReadmeWhenNothingIsPublished() = runTest {
        assertNull(ReadmeService(FakePublishedContentClient()).getReadme())
    }

    @Test
    fun servesNoReadmeWhenTheFetchFails() = runTest {
        assertNull(ReadmeService(FakePublishedContentClient(readme = null)).getReadme())
    }

    @Test
    fun servesPublishedTerminalCommands() = runTest {
        val service = TerminalCommandsService(
            FakePublishedContentClient(terminalCommands = PublishedResult.Found(PublishedTerminalCommandsFixture)),
        )

        assertEquals(PublishedTerminalCommandsFixture, service.getTerminalCommands())
    }

    @Test
    fun servesNoTerminalCommandsWhenNothingIsPublished() = runTest {
        assertNull(TerminalCommandsService(FakePublishedContentClient()).getTerminalCommands())
    }

    @Test
    fun servesNoTerminalCommandsWhenTheFetchFails() = runTest {
        assertNull(TerminalCommandsService(FakePublishedContentClient(terminalCommands = null)).getTerminalCommands())
    }
}

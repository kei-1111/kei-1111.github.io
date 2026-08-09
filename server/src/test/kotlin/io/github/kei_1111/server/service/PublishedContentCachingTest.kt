package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.client.PublishedProfile
import io.github.kei_1111.server.client.PublishedResult
import io.github.kei_1111.server.content.DefaultWorks
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Readme
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

private class CountingPublishedContentClient(
    private val works: () -> PublishedResult<Works>?,
) : PublishedContentClient {
    var worksFetchCount = 0

    override suspend fun fetchWorks(): PublishedResult<Works>? {
        worksFetchCount++
        return works()
    }

    override suspend fun fetchProfile(): PublishedResult<PublishedProfile>? = PublishedResult.Missing
    override suspend fun fetchReadme(): PublishedResult<Readme>? = PublishedResult.Missing
    override suspend fun fetchTerminalCommands(): PublishedResult<TerminalTextCommands>? = PublishedResult.Missing
}

class PublishedContentCachingTest {

    @Test
    fun servesTheSecondCallFromTheCacheWithoutRefetching() = runTest {
        val client = CountingPublishedContentClient(works = { PublishedResult.Found(publishedWorks) })
        val service = WorksService(publishedContentClient = client)

        val first = service.getWorks()
        val second = service.getWorks()

        assertEquals(first, second)
        assertEquals(1, client.worksFetchCount)
    }

    @Test
    fun suppressesTheImmediateRetryAfterAFailedFetch() = runTest {
        val client = CountingPublishedContentClient(works = { null })
        val service = WorksService(publishedContentClient = client)

        assertEquals(DefaultWorks, service.getWorks())
        assertEquals(DefaultWorks, service.getWorks())

        // 失敗直後の再取得は TtlCache の retry 抑止が抑える(フォールバック配信は継続)
        assertEquals(1, client.worksFetchCount)
    }
}

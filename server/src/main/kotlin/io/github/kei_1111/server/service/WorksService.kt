package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.client.PublishedResult
import io.github.kei_1111.server.client.valueOrNull
import io.github.kei_1111.server.content.DefaultWorks
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.Works

internal class WorksService(private val publishedContentClient: PublishedContentClient) {
    private val publishedCache =
        TtlCache<PublishedResult<Works>>(PUBLISHED_CONTENT_TTL_MILLIS, name = "published-works")

    suspend fun getWorks(): Works =
        publishedCache.get { publishedContentClient.fetchWorks() }.valueOrNull() ?: DefaultWorks
}

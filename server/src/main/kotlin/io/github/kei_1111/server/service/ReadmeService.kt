package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.client.PublishedResult
import io.github.kei_1111.server.client.valueOrNull
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.Readme

internal class ReadmeService(private val publishedContentClient: PublishedContentClient) {
    private val publishedCache =
        TtlCache<PublishedResult<Readme>>(PUBLISHED_CONTENT_TTL_MILLIS, name = "published-readme")

    suspend fun getReadme(): Readme? =
        publishedCache.get { publishedContentClient.fetchReadme() }.valueOrNull()
}

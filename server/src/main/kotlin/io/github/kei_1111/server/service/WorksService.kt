package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.content.DefaultWorks
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.Works

class WorksService(private val publishedContentClient: PublishedContentClient) {
    private val publishedCache = TtlCache<Works>(PUBLISHED_TTL_MILLIS, name = "published-works")

    suspend fun getWorks(): Works =
        publishedCache.get { publishedContentClient.fetchWorks() } ?: DefaultWorks

    companion object {
        // コンテンツ更新は低頻度のため、GCS 読み出しを抑えつつ公開後数分で反映される鮮度に保つ TTL。
        private const val PUBLISHED_TTL_MILLIS = 5L * 60L * 1000L
    }
}

package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.content.DefaultTerminalTextCommands
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.TerminalTextCommands

class TerminalCommandsService(private val publishedContentClient: PublishedContentClient) {
    private val publishedCache =
        TtlCache<TerminalTextCommands>(PUBLISHED_TTL_MILLIS, name = "published-terminal-commands")

    suspend fun getTerminalCommands(): TerminalTextCommands =
        publishedCache.get { publishedContentClient.fetchTerminalCommands() } ?: DefaultTerminalTextCommands

    companion object {
        // コンテンツ更新は低頻度のため、GCS 読み出しを抑えつつ公開後数分で反映される鮮度に保つ TTL。
        private const val PUBLISHED_TTL_MILLIS = 5L * 60L * 1000L
    }
}

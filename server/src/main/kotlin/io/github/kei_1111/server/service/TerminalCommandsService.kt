package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.client.PublishedResult
import io.github.kei_1111.server.client.valueOrNull
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.TerminalTextCommands

internal class TerminalCommandsService(private val publishedContentClient: PublishedContentClient) {
    private val publishedCache =
        TtlCache<PublishedResult<TerminalTextCommands>>(
            PUBLISHED_CONTENT_TTL_MILLIS,
            name = "published-terminal-commands",
        )

    suspend fun getTerminalCommands(): TerminalTextCommands? =
        publishedCache.get { publishedContentClient.fetchTerminalCommands() }.valueOrNull()
}

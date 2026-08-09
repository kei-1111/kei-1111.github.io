package io.github.kei_1111.app.core.api.terminal

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.app.core.api.network.getOrNull
import io.github.kei_1111.shared.model.TerminalTextCommands
import io.ktor.client.HttpClient

interface TerminalCommandsApi {
    suspend fun fetchTerminalCommands(): TerminalTextCommands?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class TerminalCommandsApiImpl(
    private val client: HttpClient,
) : TerminalCommandsApi {

    override suspend fun fetchTerminalCommands(): TerminalTextCommands? =
        client.getOrNull("$API_BASE_URL/api/terminal-commands")
}

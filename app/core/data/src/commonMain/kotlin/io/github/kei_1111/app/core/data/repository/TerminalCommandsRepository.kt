package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.terminal.TerminalCommandsApi
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.cache.SingleFlightCache
import io.github.kei_1111.shared.model.TerminalTextCommands
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface TerminalCommandsRepository {
    val terminalCommands: Flow<TerminalTextCommands>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class TerminalCommandsRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    terminalCommandsApi: TerminalCommandsApi,
) : TerminalCommandsRepository {

    private val cache = SingleFlightCache(defaultDispatcher) {
        terminalCommandsApi.fetchTerminalCommands()
    }

    override val terminalCommands: Flow<TerminalTextCommands> = flow {
        val terminalCommands = checkNotNull(cache.get()) { "terminal commands fetch failed" }
        emit(terminalCommands)
    }.flowOn(defaultDispatcher)
}

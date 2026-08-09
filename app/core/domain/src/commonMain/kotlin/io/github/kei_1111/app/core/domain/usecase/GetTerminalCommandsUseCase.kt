package io.github.kei_1111.app.core.domain.usecase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.data.repository.TerminalCommandsRepository
import io.github.kei_1111.shared.model.TerminalTextCommands
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface GetTerminalCommandsUseCase {
    operator fun invoke(): Flow<TerminalTextCommands>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetTerminalCommandsUseCaseImpl(
    private val terminalCommandsRepository: TerminalCommandsRepository,
) : GetTerminalCommandsUseCase {
    override fun invoke(): Flow<TerminalTextCommands> =
        terminalCommandsRepository.terminalCommands
            .distinctUntilChanged()
}

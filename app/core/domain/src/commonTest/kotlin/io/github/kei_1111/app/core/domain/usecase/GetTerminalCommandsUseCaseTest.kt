package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.TerminalCommandsRepository
import io.github.kei_1111.shared.model.TerminalTextCommand
import io.github.kei_1111.shared.model.TerminalTextCommands
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTerminalCommandsUseCaseTest {

    @Test
    fun forwardsTheRepositoryFlow() = runTest {
        val expected = terminalCommands(keyword = "neofetch")
        val useCase = GetTerminalCommandsUseCaseImpl(FakeTerminalCommandsRepository(flowOf(expected)))

        val actual = useCase().toList()

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun collapsesConsecutiveDuplicateEmissions() = runTest {
        val first = terminalCommands(keyword = "first")
        val duplicate = terminalCommands(keyword = "first")
        val second = terminalCommands(keyword = "second")
        val useCase = GetTerminalCommandsUseCaseImpl(
            FakeTerminalCommandsRepository(flowOf(first, duplicate, second, first)),
        )

        val actual = useCase().toList()

        assertEquals(listOf(first, second, first), actual)
    }
}

private class FakeTerminalCommandsRepository(
    override val terminalCommands: Flow<TerminalTextCommands>,
) : TerminalCommandsRepository

private fun terminalCommands(keyword: String) = TerminalTextCommands(
    items = persistentListOf(
        TerminalTextCommand(
            keyword = keyword,
            description = "test command",
            lines = persistentListOf("test output"),
        ),
    ),
)

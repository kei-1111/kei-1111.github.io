package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.api.terminal.TerminalCommandsApi
import io.github.kei_1111.shared.model.TerminalTextCommand
import io.github.kei_1111.shared.model.TerminalTextCommands
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class TerminalCommandsRepositoryImplTest {

    @Test
    fun emitsTheFetchedValue() = runTest {
        val expected = terminalCommands()
        val repository = TerminalCommandsRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            terminalCommandsApi = FakeTerminalCommandsApi(expected),
        )

        val actual = repository.terminalCommands.first()

        assertEquals(expected, actual)
    }

    @Test
    fun throwsWhenTheFetchFails() = runTest {
        val repository = TerminalCommandsRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            terminalCommandsApi = FakeTerminalCommandsApi(null),
        )

        assertFailsWith<IllegalStateException> {
            repository.terminalCommands.first()
        }
    }

    @Test
    fun emitsAnEmptyListAsASuccess() = runTest {
        val expected = TerminalTextCommands(items = persistentListOf())
        val repository = TerminalCommandsRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            terminalCommandsApi = FakeTerminalCommandsApi(expected),
        )

        val actual = repository.terminalCommands.first()

        assertEquals(expected, actual)
    }

    @Test
    fun sharesOneFetchAcrossCollections() = runTest {
        val api = FakeTerminalCommandsApi(terminalCommands())
        val repository = TerminalCommandsRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            terminalCommandsApi = api,
        )

        repository.terminalCommands.first()
        repository.terminalCommands.first()

        assertEquals(1, api.callCount)
    }
}

private class FakeTerminalCommandsApi(
    private val result: TerminalTextCommands?,
) : TerminalCommandsApi {
    var callCount = 0

    override suspend fun fetchTerminalCommands(): TerminalTextCommands? {
        callCount += 1
        return result
    }
}

private fun terminalCommands() = TerminalTextCommands(
    items = persistentListOf(
        TerminalTextCommand(
            keyword = "neofetch",
            description = "show portfolio system info",
            lines = persistentListOf("test output"),
        ),
    ),
)

package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.local.theme.ThemeLocalDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeRepositoryImplTest {

    @Test
    fun passesThroughSavedValueFromTheDataSource() = runTest {
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            themeLocalDataSource = FakeThemeLocalDataSource(flowOf(false)),
        )

        val isDark = repository.isDark.first()

        assertFalse(isDark)
    }

    @Test
    fun fallsBackToDefaultDarkThemeWhenNothingIsStored() = runTest {
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            themeLocalDataSource = FakeThemeLocalDataSource(flowOf(null)),
        )

        val isDark = repository.isDark.first()

        assertTrue(isDark)
    }

    @Test
    fun delegatesSavingToTheDataSource() = runTest {
        val dataSource = FakeThemeLocalDataSource(flowOf(null))
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            themeLocalDataSource = dataSource,
        )

        repository.saveIsDark(false)

        assertEquals(listOf(false), dataSource.savedValues)
    }
}

private class FakeThemeLocalDataSource(
    override val isDark: Flow<Boolean?>,
) : ThemeLocalDataSource {
    val savedValues = mutableListOf<Boolean>()

    override suspend fun saveIsDark(isDark: Boolean) {
        savedValues += isDark
    }
}

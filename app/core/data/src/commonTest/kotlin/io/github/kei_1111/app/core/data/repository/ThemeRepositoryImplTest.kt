package io.github.kei_1111.app.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeRepositoryImplTest {

    @Test
    fun emitsSavedValueWhenThemeWasPersisted() = runTest {
        val dataStore = FakeThemeDataStore(
            initial = preferencesOf(booleanPreferencesKey("is_dark") to false),
        )
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            dataStore = dataStore,
            clearPersistedTheme = {},
        )

        val isDark = repository.isDark.first()

        assertFalse(isDark)
    }

    @Test
    fun fallsBackToDefaultDarkThemeWhenReadingPersistedThemeFails() = runTest {
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            dataStore = ThrowingReadThemeDataStore(),
            clearPersistedTheme = {},
        )

        val isDark = repository.isDark.first()

        assertTrue(isDark)
    }

    @Test
    fun dropsPersistedThemeWhenReadingItFails() = runTest {
        var cleared = false
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            dataStore = ThrowingReadThemeDataStore(),
            clearPersistedTheme = { cleared = true },
        )

        repository.isDark.first()

        assertTrue(cleared)
    }

    @Test
    fun dropsPersistedThemeAndRetriesOnceWhenSavingFails() = runTest {
        var cleared = false
        val dataStore = FailingUpdateThemeDataStore(failingAttempts = 1)
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            dataStore = dataStore,
            clearPersistedTheme = { cleared = true },
        )

        repository.saveIsDark(false)

        assertTrue(cleared)
        assertFalse(dataStore.data.first()[booleanPreferencesKey("is_dark")] ?: true)
    }

    @Test
    fun keepsDefaultFallbackWhenDroppingPersistedThemeFails() = runTest {
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            dataStore = ThrowingReadThemeDataStore(),
            clearPersistedTheme = { error("removeItem failed") },
        )

        val isDark = repository.isDark.first()

        assertTrue(isDark)
    }

    @Test
    fun retriesSaveEvenWhenDroppingPersistedThemeFails() = runTest {
        val dataStore = FailingUpdateThemeDataStore(failingAttempts = 1)
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            dataStore = dataStore,
            clearPersistedTheme = { error("removeItem failed") },
        )

        repository.saveIsDark(false)

        assertFalse(dataStore.data.first()[booleanPreferencesKey("is_dark")] ?: true)
    }

    @Test
    fun propagatesCancellationWhileSavingWithoutDroppingPersistedTheme() = runTest {
        var cleared = false
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            dataStore = HangingUpdateThemeDataStore(),
            clearPersistedTheme = { cleared = true },
        )
        val job = launch { repository.saveIsDark(false) }
        runCurrent()

        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        assertFalse(cleared)
    }

    @Test
    fun swallowsSaveFailureWhenRetryAlsoFails() = runTest {
        val repository = ThemeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            dataStore = FailingUpdateThemeDataStore(failingAttempts = 2),
            clearPersistedTheme = {},
        )

        repository.saveIsDark(false)
    }
}

private class ThrowingReadThemeDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow {
        throw IllegalArgumentException("The last unit of input does not have enough bits")
    }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        error("updateData is not expected in this test")
}

private class HangingUpdateThemeDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> get() = MutableStateFlow(emptyPreferences())

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        awaitCancellation()
}

private class FailingUpdateThemeDataStore(
    private val failingAttempts: Int,
) : DataStore<Preferences> {
    private val state = MutableStateFlow<Preferences>(emptyPreferences())
    private var attempts = 0

    override val data: Flow<Preferences> get() = state

    // 実障害と同じ生の IllegalArgumentException を再現するため require() ではなく throw を使う
    @Suppress("UseRequire")
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        attempts++
        if (attempts <= failingAttempts) {
            throw IllegalArgumentException("The last unit of input does not have enough bits")
        }
        state.value = transform(state.value)
        return state.value
    }
}

private class FakeThemeDataStore(
    initial: Preferences,
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Preferences> get() = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        state.value = transform(state.value)
        return state.value
    }
}

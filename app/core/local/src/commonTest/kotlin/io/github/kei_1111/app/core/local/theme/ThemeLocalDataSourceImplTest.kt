package io.github.kei_1111.app.core.local.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ThemeLocalDataSourceImplTest {

    @Test
    fun emitsSavedValueWhenThemeWasPersisted() = runTest {
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = FakeThemeDataStore(
                initial = preferencesOf(booleanPreferencesKey("is_dark") to false),
            ),
            clearPersistedTheme = {},
        )

        val isDark = dataSource.isDark.first()

        assertFalse(isDark!!)
    }

    @Test
    fun emitsNullWhenReadingPersistedThemeFails() = runTest {
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = ThrowingReadThemeDataStore(),
            clearPersistedTheme = {},
        )

        val isDark = dataSource.isDark.first()

        assertNull(isDark)
    }

    @Test
    fun dropsPersistedThemeWhenReadingItFails() = runTest {
        var cleared = false
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = ThrowingReadThemeDataStore(),
            clearPersistedTheme = { cleared = true },
        )

        dataSource.isDark.first()

        assertTrue(cleared)
    }

    @Test
    fun dropsPersistedThemeAndRetriesOnceWhenSavingFails() = runTest {
        var cleared = false
        val dataStore = FailingUpdateThemeDataStore(failingAttempts = 1)
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedTheme = { cleared = true },
        )

        dataSource.saveIsDark(false)

        assertTrue(cleared)
        assertFalse(dataStore.data.first()[booleanPreferencesKey("is_dark")] ?: true)
    }

    @Test
    fun emitsNullEvenWhenDroppingPersistedThemeFails() = runTest {
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = ThrowingReadThemeDataStore(),
            clearPersistedTheme = { error("removeItem failed") },
        )

        val isDark = dataSource.isDark.first()

        assertNull(isDark)
    }

    @Test
    fun retriesSaveEvenWhenDroppingPersistedThemeFails() = runTest {
        val dataStore = FailingUpdateThemeDataStore(failingAttempts = 1)
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedTheme = { error("removeItem failed") },
        )

        dataSource.saveIsDark(false)

        assertFalse(dataStore.data.first()[booleanPreferencesKey("is_dark")] ?: true)
    }

    @Test
    fun propagatesCancellationWhileSavingWithoutDroppingPersistedTheme() = runTest {
        var cleared = false
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = HangingUpdateThemeDataStore(),
            clearPersistedTheme = { cleared = true },
        )
        val job = launch { dataSource.saveIsDark(false) }
        runCurrent()

        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        assertFalse(cleared)
    }

    @Test
    fun swallowsSaveFailureWhenRetryAlsoFails() = runTest {
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = FailingUpdateThemeDataStore(failingAttempts = 2),
            clearPersistedTheme = {},
        )

        dataSource.saveIsDark(false)
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

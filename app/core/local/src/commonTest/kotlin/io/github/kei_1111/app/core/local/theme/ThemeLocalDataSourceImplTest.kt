package io.github.kei_1111.app.core.local.theme

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import io.github.kei_1111.app.core.local.fake.FailingUpdateSettingsDataStore
import io.github.kei_1111.app.core.local.fake.FakeSettingsDataStore
import io.github.kei_1111.app.core.local.fake.HangingUpdateSettingsDataStore
import io.github.kei_1111.app.core.local.fake.ThrowingReadSettingsDataStore
import io.github.kei_1111.app.core.local.settings.PersistedSettingsCleaner
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ThemeLocalDataSourceImplTest {

    @Test
    fun emitsSavedValueWhenThemeWasPersisted() = runTest {
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = FakeSettingsDataStore(
                initial = preferencesOf(booleanPreferencesKey("is_dark") to false),
            ),
            clearPersistedSettings = PersistedSettingsCleaner {},
        )

        val isDark = dataSource.isDark.first()

        assertFalse(assertNotNull(isDark))
    }

    @Test
    fun emitsNullWhenReadingPersistedThemeFails() = runTest {
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = ThrowingReadSettingsDataStore(),
            clearPersistedSettings = PersistedSettingsCleaner {},
        )

        val isDark = dataSource.isDark.first()

        assertNull(isDark)
    }

    @Test
    fun dropsPersistedThemeWhenReadingItFails() = runTest {
        var cleared = false
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = ThrowingReadSettingsDataStore(),
            clearPersistedSettings = PersistedSettingsCleaner { cleared = true },
        )

        dataSource.isDark.first()

        assertTrue(cleared)
    }

    @Test
    fun keepsPersistedSettingsWhenASingleSaveAttemptFails() = runTest {
        var cleared = false
        val dataStore = FailingUpdateSettingsDataStore(failingAttempts = 1)
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedSettings = PersistedSettingsCleaner { cleared = true },
        )

        dataSource.saveIsDark(false)

        assertFalse(cleared)
        assertFalse(dataStore.data.first()[booleanPreferencesKey("is_dark")] ?: true)
    }

    @Test
    fun dropsPersistedThemeAndRetriesWhenEverySaveAttemptFails() = runTest {
        var cleared = false
        val dataStore = FailingUpdateSettingsDataStore(failingAttempts = 2)
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedSettings = PersistedSettingsCleaner { cleared = true },
        )

        dataSource.saveIsDark(false)

        assertTrue(cleared)
        assertFalse(dataStore.data.first()[booleanPreferencesKey("is_dark")] ?: true)
    }

    @Test
    fun emitsNullEvenWhenDroppingPersistedThemeFails() = runTest {
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = ThrowingReadSettingsDataStore(),
            clearPersistedSettings = PersistedSettingsCleaner { error("removeItem failed") },
        )

        val isDark = dataSource.isDark.first()

        assertNull(isDark)
    }

    @Test
    fun retriesSaveEvenWhenDroppingPersistedThemeFails() = runTest {
        val dataStore = FailingUpdateSettingsDataStore(failingAttempts = 2)
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedSettings = PersistedSettingsCleaner { error("removeItem failed") },
        )

        dataSource.saveIsDark(false)

        assertFalse(dataStore.data.first()[booleanPreferencesKey("is_dark")] ?: true)
    }

    @Test
    fun propagatesCancellationWhileSavingWithoutDroppingPersistedTheme() = runTest {
        var cleared = false
        val dataSource = ThemeLocalDataSourceImpl(
            dataStore = HangingUpdateSettingsDataStore(),
            clearPersistedSettings = PersistedSettingsCleaner { cleared = true },
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
            dataStore = FailingUpdateSettingsDataStore(failingAttempts = 3),
            clearPersistedSettings = PersistedSettingsCleaner {},
        )

        dataSource.saveIsDark(false)
    }
}

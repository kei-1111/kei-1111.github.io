package io.github.kei_1111.app.core.local.language

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LanguageLocalDataSourceImplTest {

    @Test
    fun emitsSavedValueWhenLanguageWasPersisted() = runTest {
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = FakeSettingsDataStore(
                initial = preferencesOf(stringPreferencesKey("language_tag") to "en"),
            ),
            clearPersistedSettings = PersistedSettingsCleaner {},
        )

        val languageTag = dataSource.languageTag.first()

        assertEquals("en", languageTag)
    }

    @Test
    fun emitsNullWhenNoLanguageWasPersisted() = runTest {
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = FakeSettingsDataStore(initial = emptyPreferences()),
            clearPersistedSettings = PersistedSettingsCleaner {},
        )

        val languageTag = dataSource.languageTag.first()

        assertNull(languageTag)
    }

    @Test
    fun emitsNullWhenReadingPersistedLanguageFails() = runTest {
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = ThrowingReadSettingsDataStore(),
            clearPersistedSettings = PersistedSettingsCleaner {},
        )

        val languageTag = dataSource.languageTag.first()

        assertNull(languageTag)
    }

    @Test
    fun dropsPersistedSettingsWhenReadingLanguageFails() = runTest {
        var cleared = false
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = ThrowingReadSettingsDataStore(),
            clearPersistedSettings = PersistedSettingsCleaner { cleared = true },
        )

        dataSource.languageTag.first()

        assertTrue(cleared)
    }

    @Test
    fun persistsTheGivenLanguageTag() = runTest {
        val dataStore = FakeSettingsDataStore(initial = emptyPreferences())
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedSettings = PersistedSettingsCleaner {},
        )

        dataSource.saveLanguageTag("en")

        assertEquals("en", dataStore.data.first()[stringPreferencesKey("language_tag")])
    }

    @Test
    fun keepsPersistedSettingsWhenASingleSaveAttemptFails() = runTest {
        var cleared = false
        val dataStore = FailingUpdateSettingsDataStore(failingAttempts = 1)
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedSettings = PersistedSettingsCleaner { cleared = true },
        )

        dataSource.saveLanguageTag("en")

        assertFalse(cleared)
        assertEquals("en", dataStore.data.first()[stringPreferencesKey("language_tag")])
    }

    @Test
    fun dropsPersistedSettingsAndRetriesWhenEverySaveAttemptFails() = runTest {
        var cleared = false
        val dataStore = FailingUpdateSettingsDataStore(failingAttempts = 2)
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedSettings = PersistedSettingsCleaner { cleared = true },
        )

        dataSource.saveLanguageTag("en")

        assertTrue(cleared)
        assertEquals("en", dataStore.data.first()[stringPreferencesKey("language_tag")])
    }

    @Test
    fun emitsNullEvenWhenDroppingPersistedSettingsFails() = runTest {
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = ThrowingReadSettingsDataStore(),
            clearPersistedSettings = PersistedSettingsCleaner { error("removeItem failed") },
        )

        val languageTag = dataSource.languageTag.first()

        assertNull(languageTag)
    }

    @Test
    fun retriesSaveEvenWhenDroppingPersistedSettingsFails() = runTest {
        val dataStore = FailingUpdateSettingsDataStore(failingAttempts = 2)
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedSettings = PersistedSettingsCleaner { error("removeItem failed") },
        )

        dataSource.saveLanguageTag("en")

        assertEquals("en", dataStore.data.first()[stringPreferencesKey("language_tag")])
    }

    @Test
    fun propagatesCancellationWhileSavingWithoutDroppingPersistedSettings() = runTest {
        var cleared = false
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = HangingUpdateSettingsDataStore(),
            clearPersistedSettings = PersistedSettingsCleaner { cleared = true },
        )
        val job = launch { dataSource.saveLanguageTag("en") }
        runCurrent()

        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        assertFalse(cleared)
    }

    @Test
    fun swallowsSaveFailureWhenRetryAlsoFails() = runTest {
        val dataSource = LanguageLocalDataSourceImpl(
            dataStore = FailingUpdateSettingsDataStore(failingAttempts = 3),
            clearPersistedSettings = PersistedSettingsCleaner {},
        )

        dataSource.saveLanguageTag("en")
    }
}

package io.github.kei_1111.app.core.local.notification

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificationLocalDataSourceImplTest {

    @Test
    fun emitsSavedPrNumberWhenItWasPersisted() = runTest {
        val dataSource = NotificationLocalDataSourceImpl(
            dataStore = FakeNotificationDataStore(
                initial = preferencesOf(intPreferencesKey("last_notified_pr_number") to 206),
            ),
            clearPersistedNotification = {},
        )

        val prNumber = dataSource.lastNotifiedPrNumber.first()

        assertEquals(206, prNumber)
    }

    @Test
    fun emitsNullWhenNoPrNumberWasPersisted() = runTest {
        val dataSource = NotificationLocalDataSourceImpl(
            dataStore = FakeNotificationDataStore(initial = emptyPreferences()),
            clearPersistedNotification = {},
        )

        val prNumber = dataSource.lastNotifiedPrNumber.first()

        assertNull(prNumber)
    }

    @Test
    fun savesTheLastNotifiedPrNumber() = runTest {
        val dataStore = FakeNotificationDataStore(initial = emptyPreferences())
        val dataSource = NotificationLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedNotification = {},
        )

        dataSource.saveLastNotifiedPrNumber(206)

        assertEquals(206, dataStore.data.first()[intPreferencesKey("last_notified_pr_number")])
    }

    @Test
    fun emitsNullWhenReadingPersistedNotificationFails() = runTest {
        val dataSource = NotificationLocalDataSourceImpl(
            dataStore = ThrowingReadNotificationDataStore(),
            clearPersistedNotification = {},
        )

        val prNumber = dataSource.lastNotifiedPrNumber.first()

        assertNull(prNumber)
    }

    @Test
    fun dropsPersistedNotificationWhenReadingItFails() = runTest {
        var cleared = false
        val dataSource = NotificationLocalDataSourceImpl(
            dataStore = ThrowingReadNotificationDataStore(),
            clearPersistedNotification = { cleared = true },
        )

        dataSource.lastNotifiedPrNumber.first()

        assertTrue(cleared)
    }

    @Test
    fun dropsPersistedNotificationAndRetriesOnceWhenSavingFails() = runTest {
        var cleared = false
        val dataStore = FailingUpdateNotificationDataStore(failingAttempts = 1)
        val dataSource = NotificationLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedNotification = { cleared = true },
        )

        dataSource.saveLastNotifiedPrNumber(206)

        assertTrue(cleared)
        assertEquals(2, dataStore.updateAttempts)
        assertEquals(206, dataStore.data.first()[intPreferencesKey("last_notified_pr_number")])
    }

    @Test
    fun emitsNullEvenWhenDroppingPersistedNotificationFails() = runTest {
        val dataSource = NotificationLocalDataSourceImpl(
            dataStore = ThrowingReadNotificationDataStore(),
            clearPersistedNotification = { error("removeItem failed") },
        )

        val prNumber = dataSource.lastNotifiedPrNumber.first()

        assertNull(prNumber)
    }

    @Test
    fun retriesSaveEvenWhenDroppingPersistedNotificationFails() = runTest {
        val dataStore = FailingUpdateNotificationDataStore(failingAttempts = 1)
        val dataSource = NotificationLocalDataSourceImpl(
            dataStore = dataStore,
            clearPersistedNotification = { error("removeItem failed") },
        )

        dataSource.saveLastNotifiedPrNumber(206)

        assertEquals(206, dataStore.data.first()[intPreferencesKey("last_notified_pr_number")])
    }

    @Test
    fun propagatesCancellationWhileSavingWithoutDroppingPersistedNotification() = runTest {
        var cleared = false
        val dataSource = NotificationLocalDataSourceImpl(
            dataStore = HangingUpdateNotificationDataStore(),
            clearPersistedNotification = { cleared = true },
        )
        val job = launch { dataSource.saveLastNotifiedPrNumber(206) }
        runCurrent()

        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        assertFalse(cleared)
    }

    @Test
    fun swallowsSaveFailureWhenRetryAlsoFails() = runTest {
        val dataSource = NotificationLocalDataSourceImpl(
            dataStore = FailingUpdateNotificationDataStore(failingAttempts = 2),
            clearPersistedNotification = {},
        )

        dataSource.saveLastNotifiedPrNumber(206)
    }
}

private class ThrowingReadNotificationDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow {
        throw IllegalArgumentException("The last unit of input does not have enough bits")
    }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        error("updateData is not expected in this test")
}

private class HangingUpdateNotificationDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> get() = MutableStateFlow(emptyPreferences())

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        awaitCancellation()
}

private class FailingUpdateNotificationDataStore(
    private val failingAttempts: Int,
) : DataStore<Preferences> {
    private val state = MutableStateFlow<Preferences>(emptyPreferences())
    var updateAttempts = 0
        private set

    override val data: Flow<Preferences> get() = state

    @Suppress("UseRequire")
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        updateAttempts++
        if (updateAttempts <= failingAttempts) {
            throw IllegalArgumentException("The last unit of input does not have enough bits")
        }
        state.value = transform(state.value)
        return state.value
    }
}

private class FakeNotificationDataStore(
    initial: Preferences,
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Preferences> get() = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        state.value = transform(state.value)
        return state.value
    }
}

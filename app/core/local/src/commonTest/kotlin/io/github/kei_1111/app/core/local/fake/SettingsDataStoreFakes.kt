package io.github.kei_1111.app.core.local.fake

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

internal class ThrowingReadSettingsDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow {
        throw IllegalArgumentException("The last unit of input does not have enough bits")
    }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        error("updateData is not expected in this test")
}

internal class HangingUpdateSettingsDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> get() = MutableStateFlow(emptyPreferences())

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        awaitCancellation()
}

internal class FailingUpdateSettingsDataStore(
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

internal class FakeSettingsDataStore(
    initial: Preferences,
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Preferences> get() = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        state.value = transform(state.value)
        return state.value
    }
}

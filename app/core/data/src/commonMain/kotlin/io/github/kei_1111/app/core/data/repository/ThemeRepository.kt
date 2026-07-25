package io.github.kei_1111.app.core.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.theme.createThemeDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface ThemeRepository {
    /** 保存されたテーマ選択。未保存時は初期値のダーク（true）。 */
    val isDark: Flow<Boolean>

    suspend fun saveIsDark(isDark: Boolean)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ThemeRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ThemeRepository {

    private val dataStore = createThemeDataStore()

    override val isDark: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DARK_KEY] ?: DEFAULT_IS_DARK
    }.flowOn(defaultDispatcher)

    override suspend fun saveIsDark(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_KEY] = isDark
        }
    }

    private companion object {
        val IS_DARK_KEY = booleanPreferencesKey("is_dark")
        const val DEFAULT_IS_DARK = true
    }
}

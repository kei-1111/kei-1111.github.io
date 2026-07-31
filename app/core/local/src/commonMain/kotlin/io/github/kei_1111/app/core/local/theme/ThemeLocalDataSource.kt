package io.github.kei_1111.app.core.local.theme

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ThemeLocalDataSource {
    /** 保存されたテーマ選択。未保存時は null。 */
    val isDark: Flow<Boolean?>

    suspend fun saveIsDark(isDark: Boolean)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ThemeLocalDataSourceImpl : ThemeLocalDataSource {

    private val dataStore = createThemeDataStore()

    override val isDark: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[IS_DARK_KEY]
    }

    override suspend fun saveIsDark(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_KEY] = isDark
        }
    }

    private companion object {
        val IS_DARK_KEY = booleanPreferencesKey("is_dark")
    }
}

package io.github.kei_1111.app.core.local.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.coroutines.recoverOrElse
import io.github.kei_1111.app.core.common.coroutines.runBestEffort
import io.github.kei_1111.app.core.local.settings.PersistedSettingsCleaner
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface ThemeLocalDataSource {
    /** 保存されたテーマ選択。未保存時と読み取り失敗時は null。 */
    val isDark: Flow<Boolean?>

    suspend fun saveIsDark(isDark: Boolean)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ThemeLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val clearPersistedSettings: PersistedSettingsCleaner,
) : ThemeLocalDataSource {

    // 読み取り失敗は「保存値なし」（null）と同じ扱いにし、破損した保存データは破棄して次回以降を自己修復する。
    // ライブラリが CorruptionException に分類しない破損（例: 不整合な localStorage 状態の生 IllegalArgumentException）
    // が起動経路のコルーチンを殺すのを防ぐ
    override val isDark: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[IS_DARK_KEY]
    }.catch { _ ->
        currentCoroutineContext().ensureActive()
        runBestEffort { clearPersistedSettings() }
        emit(null)
    }

    // 破損した保存値は書き込みも塞ぐため、破棄してから再試行する。破棄はストアを共有する他の設定も
    // 巻き添えにするので、その前に素の再試行で一時障害を切り分ける。再失敗は握り潰す（保存は best-effort）
    override suspend fun saveIsDark(isDark: Boolean) {
        recoverOrElse({ writeIsDark(isDark) }) {
            recoverOrElse({ writeIsDark(isDark) }) {
                runBestEffort { clearPersistedSettings() }
                runBestEffort { writeIsDark(isDark) }
            }
        }
    }

    private suspend fun writeIsDark(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_KEY] = isDark
        }
    }

    private companion object {
        val IS_DARK_KEY = booleanPreferencesKey("is_dark")
    }
}

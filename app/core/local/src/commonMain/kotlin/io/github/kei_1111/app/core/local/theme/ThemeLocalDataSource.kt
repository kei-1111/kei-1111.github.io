package io.github.kei_1111.app.core.local.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
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
// プライマリコンストラクタはテスト注入用のシーム。DI は @Inject 付きセカンダリコンストラクタだけを通る
// （デフォルト引数のシームは Metro がグラフ依存と解釈しモジュール間でファクトリ署名が食い違うため不可）
internal class ThemeLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val clearPersistedTheme: () -> Unit,
) : ThemeLocalDataSource {

    @Inject
    constructor() : this(
        dataStore = createThemeDataStore(),
        clearPersistedTheme = ::clearThemeDataStore,
    )

    // 読み取り失敗は「保存値なし」（null）と同じ扱いにし、破損した保存データは破棄して次回以降を自己修復する。
    // ライブラリが CorruptionException に分類しない破損（例: 不整合な localStorage 状態の生 IllegalArgumentException）
    // が起動経路のコルーチンを殺すのを防ぐ
    override val isDark: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[IS_DARK_KEY]
    }.catch { _ ->
        currentCoroutineContext().ensureActive()
        dropPersistedThemeQuietly()
        emit(null)
    }

    // 破損した保存値は書き込みも塞ぐため、破棄してから一度だけ再試行する。再失敗は握り潰す（保存は best-effort）
    override suspend fun saveIsDark(isDark: Boolean) {
        try {
            writeIsDark(isDark)
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            dropPersistedThemeQuietly()
            try {
                writeIsDark(isDark)
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
            }
        }
    }

    // クリア自体の失敗（storage アクセス不可など）が回復経路から漏れて凍結を再発させないよう握り潰す
    private suspend fun dropPersistedThemeQuietly() {
        try {
            clearPersistedTheme()
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
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

package io.github.kei_1111.app.core.local.language

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

interface LanguageLocalDataSource {
    /** 保存された表示言語の BCP 47 タグ。未保存時と読み取り失敗時は null。 */
    val languageTag: Flow<String?>

    suspend fun saveLanguageTag(languageTag: String)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class LanguageLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val clearPersistedSettings: PersistedSettingsCleaner,
) : LanguageLocalDataSource {

    override val languageTag: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_TAG_KEY]
    }.catch { _ ->
        currentCoroutineContext().ensureActive()
        runBestEffort { clearPersistedSettings() }
        emit(null)
    }

    // 破損した保存値は書き込みも塞ぐため、破棄してから再試行する。破棄はストアを共有する他の設定も
    // 巻き添えにするので、その前に素の再試行で一時障害を切り分ける。再失敗は握り潰す（保存は best-effort）
    override suspend fun saveLanguageTag(languageTag: String) {
        recoverOrElse({ writeLanguageTag(languageTag) }) {
            recoverOrElse({ writeLanguageTag(languageTag) }) {
                runBestEffort { clearPersistedSettings() }
                runBestEffort { writeLanguageTag(languageTag) }
            }
        }
    }

    private suspend fun writeLanguageTag(languageTag: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_TAG_KEY] = languageTag
        }
    }

    private companion object {
        val LANGUAGE_TAG_KEY = stringPreferencesKey("language_tag")
    }
}

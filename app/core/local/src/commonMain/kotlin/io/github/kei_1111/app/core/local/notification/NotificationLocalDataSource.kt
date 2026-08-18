package io.github.kei_1111.app.core.local.notification

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.coroutines.recoverOrElse
import io.github.kei_1111.app.core.common.coroutines.runBestEffort
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface NotificationLocalDataSource {
    /** 最後に通知した PR 番号。未保存時と読み取り失敗時は null。 */
    val lastNotifiedPrNumber: Flow<Int?>

    suspend fun saveLastNotifiedPrNumber(prNumber: Int)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
// プライマリコンストラクタはテスト注入用のシーム。DI は @Inject 付きセカンダリコンストラクタだけを通る
// （デフォルト引数のシームは Metro がグラフ依存と解釈しモジュール間でファクトリ署名が食い違うため不可）
internal class NotificationLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val clearPersistedNotification: () -> Unit,
) : NotificationLocalDataSource {

    @Inject
    constructor() : this(
        dataStore = createNotificationDataStore(),
        clearPersistedNotification = ::clearNotificationDataStore,
    )

    // 読み取り失敗は「保存値なし」（null）と同じ扱いにし、破損した保存データは破棄して次回以降を自己修復する。
    // ライブラリが CorruptionException に分類しない破損（例: 不整合な localStorage 状態の生 IllegalArgumentException）
    // が起動経路のコルーチンを殺すのを防ぐ
    override val lastNotifiedPrNumber: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[LAST_NOTIFIED_PR_NUMBER_KEY]
    }.catch { _ ->
        currentCoroutineContext().ensureActive()
        runBestEffort { clearPersistedNotification() }
        emit(null)
    }

    // 破損した保存値は書き込みも塞ぐため、破棄してから一度だけ再試行する。再失敗は握り潰す（保存は best-effort）
    override suspend fun saveLastNotifiedPrNumber(prNumber: Int) {
        recoverOrElse({ writeLastNotifiedPrNumber(prNumber) }) {
            runBestEffort { clearPersistedNotification() }
            runBestEffort { writeLastNotifiedPrNumber(prNumber) }
        }
    }

    private suspend fun writeLastNotifiedPrNumber(prNumber: Int) {
        dataStore.edit { preferences ->
            preferences[LAST_NOTIFIED_PR_NUMBER_KEY] = prNumber
        }
    }

    private companion object {
        val LAST_NOTIFIED_PR_NUMBER_KEY = intPreferencesKey("last_notified_pr_number")
    }
}

package io.github.kei_1111.app.core.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** 設定 DataStore の名前。wasmJs では localStorage のキーとして使われる。 */
internal const val SETTINGS_DATA_STORE_NAME = "settings.preferences_pb"

/** プラットフォーム毎の DataStore 生成。Android actual は Preview からもホストテストからも呼ばれない（コンパイル用スタブ）。 */
internal expect fun createSettingsDataStore(): DataStore<Preferences>

/** 保存済み設定を破棄する。読み書きに失敗した破損データを取り除いて自己修復するために使う。 */
internal expect fun clearSettingsDataStore()

/** [clearSettingsDataStore] を関数参照で直に呼ぶとテストで差し替えられないため、型として配る。 */
fun interface PersistedSettingsCleaner {
    operator fun invoke()
}

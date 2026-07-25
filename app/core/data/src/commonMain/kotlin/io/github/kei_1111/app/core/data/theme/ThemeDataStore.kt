package io.github.kei_1111.app.core.data.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** テーマ設定 DataStore の名前。wasmJs では localStorage のキーとして使われる。 */
internal const val THEME_DATA_STORE_NAME = "theme.preferences_pb"

/** プラットフォーム毎の DataStore 生成。Android は Preview 専用のため実行されない（コンパイル用スタブ）。 */
internal expect fun createThemeDataStore(): DataStore<Preferences>

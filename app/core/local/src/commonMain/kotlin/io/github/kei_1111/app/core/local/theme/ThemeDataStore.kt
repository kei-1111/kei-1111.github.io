package io.github.kei_1111.app.core.local.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** テーマ設定 DataStore の名前。wasmJs では localStorage のキーとして使われる。 */
internal const val THEME_DATA_STORE_NAME = "theme.preferences_pb"

/** プラットフォーム毎の DataStore 生成。Android は Preview 専用のため実行されない（コンパイル用スタブ）。 */
internal expect fun createThemeDataStore(): DataStore<Preferences>

/** 保存済みテーマを破棄する。読み書きに失敗した破損データを取り除いて自己修復するために使う。 */
internal expect fun clearThemeDataStore()

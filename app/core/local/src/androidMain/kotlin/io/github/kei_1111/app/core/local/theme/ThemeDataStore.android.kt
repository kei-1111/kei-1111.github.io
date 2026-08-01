package io.github.kei_1111.app.core.local.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** Android ターゲットは IDE Preview 専用で、Preview は Repository を生成しないため呼び出しは想定外（コンパイル用スタブ）。 */
internal actual fun createThemeDataStore(): DataStore<Preferences> =
    error("createThemeDataStore is not supported on the preview-only Android target")

/** Android ターゲットは IDE Preview 専用で、Preview は Repository を生成しないため呼び出しは想定外（コンパイル用スタブ）。 */
internal actual fun clearThemeDataStore(): Unit =
    error("clearThemeDataStore is not supported on the preview-only Android target")

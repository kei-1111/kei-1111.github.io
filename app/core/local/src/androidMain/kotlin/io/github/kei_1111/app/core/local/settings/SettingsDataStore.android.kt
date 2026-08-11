package io.github.kei_1111.app.core.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** Preview もホストテストも Repository を生成しないため呼び出しは想定外（コンパイル用スタブ）。 */
internal actual fun createSettingsDataStore(): DataStore<Preferences> =
    error("createSettingsDataStore is not supported on the non-shipped Android target")

/** Preview もホストテストも Repository を生成しないため呼び出しは想定外（コンパイル用スタブ）。 */
internal actual fun clearSettingsDataStore(): Unit =
    error("clearSettingsDataStore is not supported on the non-shipped Android target")

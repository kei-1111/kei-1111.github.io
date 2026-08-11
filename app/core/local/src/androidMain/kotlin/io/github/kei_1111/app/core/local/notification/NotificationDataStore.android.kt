package io.github.kei_1111.app.core.local.notification

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** Preview もホストテストも Repository を生成しないため呼び出しは想定外（コンパイル用スタブ）。 */
internal actual fun createNotificationDataStore(): DataStore<Preferences> =
    error("createNotificationDataStore is not supported on the non-shipped Android target")

/** Preview もホストテストも Repository を生成しないため呼び出しは想定外（コンパイル用スタブ）。 */
internal actual fun clearNotificationDataStore(): Unit =
    error("clearNotificationDataStore is not supported on the non-shipped Android target")

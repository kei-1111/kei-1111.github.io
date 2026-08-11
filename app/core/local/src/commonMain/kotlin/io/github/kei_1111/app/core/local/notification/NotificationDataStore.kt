package io.github.kei_1111.app.core.local.notification

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** 通知設定 DataStore の名前。wasmJs では localStorage のキーとして使われる。 */
internal const val NOTIFICATION_DATA_STORE_NAME = "notification.preferences_pb"

/** プラットフォーム毎の DataStore 生成。Android actual は Preview からもホストテストからも呼ばれない（コンパイル用スタブ）。 */
internal expect fun createNotificationDataStore(): DataStore<Preferences>

/** 保存済み通知設定を破棄する。読み書きに失敗した破損データを取り除いて自己修復するために使う。 */
internal expect fun clearNotificationDataStore()

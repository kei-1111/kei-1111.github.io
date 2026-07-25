package io.github.kei_1111.app.core.data.theme

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.emptyPreferences

/** ブラウザの localStorage に永続化する。破損した保存データは空の Preferences に置き換えて自己修復する。 */
internal actual fun createThemeDataStore(): DataStore<Preferences> =
    DataStoreFactory.create(
        storage = WebLocalStorage(
            serializer = PreferencesSerializer,
            name = THEME_DATA_STORE_NAME,
        ),
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    )

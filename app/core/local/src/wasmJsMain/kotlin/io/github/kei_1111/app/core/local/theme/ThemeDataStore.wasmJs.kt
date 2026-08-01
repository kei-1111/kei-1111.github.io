package io.github.kei_1111.app.core.local.theme

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.emptyPreferences

internal actual fun createThemeDataStore(): DataStore<Preferences> =
    DataStoreFactory.create(
        storage = WebLocalStorage(
            serializer = PreferencesSerializer,
            name = THEME_DATA_STORE_NAME,
        ),
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    )

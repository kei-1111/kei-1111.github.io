package io.github.kei_1111.app.core.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
interface SettingsDataStoreBindings {

    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideSettingsDataStore(): DataStore<Preferences> = createSettingsDataStore()

        @Provides
        @SingleIn(AppScope::class)
        fun providePersistedSettingsCleaner(): PersistedSettingsCleaner =
            PersistedSettingsCleaner { clearSettingsDataStore() }
    }
}

package io.github.kei_1111.feature.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.kei_1111.feature.profile.ProfileScreen

fun EntryProviderScope<NavKey>.profileEntries() {
    entry<Profile> {
        ProfileScreen()
    }
}

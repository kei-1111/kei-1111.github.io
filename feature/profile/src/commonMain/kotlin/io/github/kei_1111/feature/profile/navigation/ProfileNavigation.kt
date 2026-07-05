package io.github.kei_1111.feature.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kei_1111.feature.profile.destination.profile.ProfileScreen
import io.github.kei_1111.feature.profile.destination.profile.ProfileViewModel

fun EntryProviderScope<NavKey>.profileEntries() {
    entry<Profile> {
        val viewModel: ProfileViewModel = metroViewModel()
        ProfileScreen(viewModel = viewModel)
    }
}

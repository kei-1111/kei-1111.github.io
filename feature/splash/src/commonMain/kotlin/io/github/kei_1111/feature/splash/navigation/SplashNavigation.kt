package io.github.kei_1111.feature.splash.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kei_1111.feature.splash.destination.splash.SplashScreen
import io.github.kei_1111.feature.splash.destination.splash.SplashViewModel

fun EntryProviderScope<NavKey>.splashEntries(
    navigateProfile: () -> Unit,
) {
    entry<Splash> {
        val viewModel: SplashViewModel = metroViewModel()
        SplashScreen(
            viewModel = viewModel,
            navigateProfile = navigateProfile,
        )
    }
}

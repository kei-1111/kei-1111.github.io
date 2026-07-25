package io.github.kei_1111.app.feature.splash.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kei_1111.app.feature.splash.destination.splash.SplashScreenRoot
import io.github.kei_1111.app.feature.splash.destination.splash.SplashViewModel

fun EntryProviderScope<NavKey>.splashEntries(
    navigateProfile: () -> Unit,
) {
    entry<Splash> {
        val viewModel: SplashViewModel = metroViewModel()
        SplashScreenRoot(
            viewModel = viewModel,
            navigateProfile = navigateProfile,
        )
    }
}

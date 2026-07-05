package io.github.kei_1111.feature.splash.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.kei_1111.feature.splash.SplashScreen

fun EntryProviderScope<NavKey>.splashEntries(
    navigateProfile: () -> Unit,
) {
    entry<Splash> {
        SplashScreen(
            toProfile = navigateProfile,
        )
    }
}

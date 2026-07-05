package io.github.kei_1111.feature.splash.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.navigateSplash() = add(Splash)

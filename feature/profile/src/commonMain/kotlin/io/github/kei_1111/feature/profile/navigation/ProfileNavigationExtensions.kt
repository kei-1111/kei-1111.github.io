package io.github.kei_1111.feature.profile.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.navigateProfile() = add(Profile)

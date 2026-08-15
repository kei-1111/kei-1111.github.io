package io.github.kei_1111.app.feature.profile.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

// Splash を残すと back で完了済みの Splash に戻り、再遷移する契機がないまま詰む
fun NavBackStack<NavKey>.navigateProfile() {
    clear()
    add(Profile)
}

fun NavBackStack<NavKey>.navigateSearchEverywhere() = add(SearchEverywhere)

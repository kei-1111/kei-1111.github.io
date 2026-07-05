@file:Suppress("MagicNumber")

package io.github.kei_1111.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.kei_1111.core.designsystem.theme.animations.Durations
import io.github.kei_1111.feature.profile.navigation.Profile
import io.github.kei_1111.feature.profile.navigation.profileEntries
import io.github.kei_1111.feature.splash.navigation.Splash
import io.github.kei_1111.feature.splash.navigation.splashEntries
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private const val NAVIGATION_INITIAL_ALPHA = 0.1f

// navigation-compose (nav2) has no built-in-default equivalent on nav3; this mirrors its
// StandardDefaultNavTransitions (fadeIn/fadeOut with tween(700)), which NavGraph.kt relied on
// implicitly for the forward Splash -> Profile transition.
private const val FORWARD_TRANSITION_DURATION = 700

// wasmJs has no reflection, so the open-polymorphic NavKey back stack must be restored via an
// explicit SerializersModule registering every NavKey subclass.
private val navKeySavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Splash::class, Splash.serializer())
            subclass(Profile::class, Profile.serializer())
        }
    }
}

@Composable
fun AppNavDisplay() {
    val backStack = rememberNavBackStack(navKeySavedStateConfiguration, Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = {
            fadeIn(animationSpec = tween(FORWARD_TRANSITION_DURATION)) togetherWith
                fadeOut(animationSpec = tween(FORWARD_TRANSITION_DURATION))
        },
        // Mirrors NavGraph.kt's popEnterTransition(Splash)/popExitTransition(Profile) — the only
        // pop path reachable in this two-screen graph is Profile -> Splash.
        popTransitionSpec = {
            fadeIn(
                animationSpec = tween(Durations.Long),
                initialAlpha = NAVIGATION_INITIAL_ALPHA,
            ) togetherWith fadeOut(animationSpec = tween(Durations.Long))
        },
        entryProvider = entryProvider {
            splashEntries(navigateProfile = { backStack.add(Profile) })
            profileEntries()
        },
    )
}

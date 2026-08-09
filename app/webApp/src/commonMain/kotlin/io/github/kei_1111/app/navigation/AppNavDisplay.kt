package io.github.kei_1111.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.kei_1111.app.core.common.logging.InteractionLog
import io.github.kei_1111.app.core.navigation.InlineDialogSceneStrategy
import io.github.kei_1111.app.core.navigation.LocalResultEventBus
import io.github.kei_1111.app.core.navigation.ResultEventBus
import io.github.kei_1111.app.core.navigation.crossFadeIn
import io.github.kei_1111.app.core.navigation.crossFadeOut
import io.github.kei_1111.app.feature.profile.navigation.SearchEverywhere
import io.github.kei_1111.app.feature.profile.navigation.navigateProfile
import io.github.kei_1111.app.feature.profile.navigation.navigateSearchEverywhere
import io.github.kei_1111.app.feature.profile.navigation.profileEntries
import io.github.kei_1111.app.feature.splash.navigation.Splash
import io.github.kei_1111.app.feature.splash.navigation.splashEntries
import kotlinx.serialization.modules.SerializersModule

@Composable
fun AppNavDisplay(
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    interactionLog: InteractionLog,
    navKeySerializers: Set<SerializersModule>,
) {
    // wasmJs has no reflection, so the open-polymorphic NavKey back stack must be restored via an
    // explicit SerializersModule; each feature contributes its fragment via Metro (@IntoSet).
    val savedStateConfiguration = remember(navKeySerializers) {
        SavedStateConfiguration {
            serializersModule = SerializersModule { navKeySerializers.forEach { include(it) } }
        }
    }
    val backStack = rememberNavBackStack(savedStateConfiguration, Splash)
    val resultEventBus = remember { ResultEventBus() }

    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    interactionLog.i("Navigation", "back")
                    backStack.removeLastOrNull()
                }
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            sceneStrategies = remember { listOf(InlineDialogSceneStrategy<NavKey>()) },
            transitionSpec = { crossFadeIn() },
            popTransitionSpec = { crossFadeOut() },
            entryProvider = entryProvider {
                splashEntries(
                    navigateProfile = {
                        interactionLog.i("Navigation", "navigate to Profile")
                        backStack.navigateProfile()
                    },
                )
                profileEntries(
                    navigateSearchEverywhere = {
                        // ダブル Shift はダイアログ表示中も届くため、多重 push を防ぐ
                        if (backStack.lastOrNull() != SearchEverywhere) {
                            interactionLog.i("Navigation", "navigate to SearchEverywhere")
                            backStack.navigateSearchEverywhere()
                        }
                    },
                    navigateBack = {
                        if (backStack.lastOrNull() == SearchEverywhere) {
                            interactionLog.i("Navigation", "back")
                            backStack.removeLastOrNull()
                        }
                    },
                    onToggleTheme = onToggleTheme,
                    onToggleLanguage = onToggleLanguage,
                )
            },
        )
    }
}

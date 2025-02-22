package io.github.kei_1111.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.kei_1111.core.designsystem.theme.animations.Durations
import io.github.kei_1111.feature.profile.ProfileScreen
import io.github.kei_1111.ui.feature.splash.SplashScreen

private const val NavigationInitialAlpha = 0.1f

@Composable
fun NavGraph(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(
            route = Screen.Splash.route,
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(Durations.Long),
                    initialAlpha = NavigationInitialAlpha,
                )
            },
            popExitTransition = { fadeOut(animationSpec = tween(Durations.Long)) },
        ) {
            SplashScreen(
                toProfile = { navController.navigate(Screen.Profile.route) },
            )
        }
        composable(
            route = Screen.Profile.route,
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(Durations.Medium),
                    initialAlpha = NavigationInitialAlpha,
                )
            },
            popExitTransition = { fadeOut(animationSpec = tween(Durations.Long)) },
        ) {
            ProfileScreen()
        }
    }
}

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Profile : Screen("profile")
}

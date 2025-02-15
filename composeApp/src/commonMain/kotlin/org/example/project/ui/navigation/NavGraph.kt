package org.example.project.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.example.project.ui.feature.profile.ProfileScreen
import org.example.project.ui.feature.splash.SplashScreen
import org.example.project.ui.theme.animations.Durations

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

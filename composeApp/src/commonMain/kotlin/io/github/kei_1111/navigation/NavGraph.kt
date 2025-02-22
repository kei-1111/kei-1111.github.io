package io.github.kei_1111.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.kei_1111.core.designsystem.theme.animations.Durations
import io.github.kei_1111.feature.profile.ProfileScreen
import io.github.kei_1111.feature.profile.navgation.Profile
import io.github.kei_1111.feature.profile.navgation.navigateToProfile
import io.github.kei_1111.feature.splash.SplashScreen
import io.github.kei_1111.feature.splash.navigation.Splash

private const val NavigationInitialAlpha = 0.1f

@Composable
fun NavGraph(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Splash,
    ) {
        composable<Splash>(
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(Durations.Long),
                    initialAlpha = NavigationInitialAlpha,
                )
            },
            popExitTransition = { fadeOut(animationSpec = tween(Durations.Long)) },
        ) {
            SplashScreen(
                toProfile = navController::navigateToProfile,
            )
        }
        composable<Profile>(
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

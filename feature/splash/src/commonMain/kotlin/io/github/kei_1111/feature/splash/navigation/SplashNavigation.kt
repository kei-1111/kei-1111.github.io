@file:Suppress("MatchingDeclarationName")

package io.github.kei_1111.feature.splash.navigation

import androidx.navigation.NavHostController
import io.github.kei_1111.core.common.Screen
import kotlinx.serialization.Serializable

@Serializable
data object Splash : Screen

fun NavHostController.navigateToSplash() = navigate(Splash)

@file:Suppress("MatchingDeclarationName")

package io.github.kei_1111.feature.profile.navgation

import androidx.navigation.NavHostController
import io.github.kei_1111.core.common.Screen
import kotlinx.serialization.Serializable

@Serializable
data object Profile : Screen

fun NavHostController.navigateToProfile() = navigate(Profile)

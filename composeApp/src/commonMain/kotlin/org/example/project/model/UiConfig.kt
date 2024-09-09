package org.example.project.model

import androidx.compose.ui.unit.dp

@Suppress("MagicNumber")
data object UiConfig {
    // Icon sizes
    val ExtraLargeIconSize = 200.dp
    val LargeIconSize = 150.dp
    val MediumIconSize = 100.dp
    val SmallIconSize = 75.dp

    // Padding values
    val ContentPadding = 50.dp
    val LargePadding = 30.dp
    val MediumPadding = 20.dp
    val SmallPadding = 10.dp
    val ExtraSmallPadding = 5.dp

    // Weight
    const val DefaultWeight = 1f

    // Mobile Width
    val MobileWidth = 600.dp

    // Splash Screen
    const val SplashLoadingAnimationRotation = 25f

    // Profile Screen
    const val ProfileDesktopRightWeight = 1.5f
    val ProfileCareerThickness = 5.dp
    val ProfileCareerDividerWidth = 15.dp
}

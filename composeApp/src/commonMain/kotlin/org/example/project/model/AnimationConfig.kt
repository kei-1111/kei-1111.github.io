package org.example.project.model

@Suppress("MagicNumber")
data object AnimationConfig {
    // Duration
    const val LongDuration = 1500
    const val MediumDuration = 1000
    const val ShortDuration = 500

    // Navigation
    const val NavigationInitialAlpha = 0.1f

    // Splash Screen
    const val SplashInitialProfileIconXOffset = -200f
    const val SplashFinalProfileIconXOffset = 0f
    const val SplashInitialProfileIconAlpha = 0f
    const val SplashFinalProfileIconAlpha = 1f
    const val SplashCharacterDisplayDelay = 100L

    // Profiel Screen
    const val ProfileRatedDuration = 3000
    const val ProfileRatedInitialAlpha = 1f
    const val ProfileRatedFinalAlpha = 0.5f
    const val ProfileRatedInitialStartOffset = 500
}

package io.github.kei_1111.feature.splash.destination.splash

import io.github.kei_1111.core.mvi.Intent

internal sealed interface SplashIntent : Intent {
    data class ReceiveFontLoaded(val font: SplashFont) : SplashIntent
    data class UpdatePageVisibility(val isVisible: Boolean) : SplashIntent
    data object ConsumeEffect : SplashIntent
}

package io.github.kei_1111.app.feature.splash.destination.splash

internal sealed interface SplashEffect {
    data object NavigateProfile : SplashEffect
}

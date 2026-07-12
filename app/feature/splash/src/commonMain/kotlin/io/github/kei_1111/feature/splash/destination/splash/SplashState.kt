package io.github.kei_1111.feature.splash.destination.splash

import io.github.kei_1111.core.mvi.State

internal data class SplashState(
    val jetBrainsMonoStep: SplashStep = SplashStep.Running,
    val notoSansJpStep: SplashStep = SplashStep.Running,
    val zenKakuGothicNewStep: SplashStep = SplashStep.Running,
    val renderStep: SplashStep = SplashStep.Running,
    val buildStatus: BuildStatus = BuildStatus.Running,
    val effect: SplashEffect? = null,
) : State

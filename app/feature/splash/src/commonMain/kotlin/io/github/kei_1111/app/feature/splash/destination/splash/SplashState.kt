package io.github.kei_1111.app.feature.splash.destination.splash

import io.github.kei_1111.app.core.mvi.State
import io.github.kei_1111.app.feature.splash.destination.splash.model.BuildStatus
import io.github.kei_1111.app.feature.splash.destination.splash.model.SplashStep

internal data class SplashState(
    val jetBrainsMonoStep: SplashStep = SplashStep.Running,
    val notoSansJpStep: SplashStep = SplashStep.Running,
    val zenKakuGothicNewStep: SplashStep = SplashStep.Running,
    val renderStep: SplashStep = SplashStep.Running,
    val buildStatus: BuildStatus = BuildStatus.Running,
    val effect: SplashEffect? = null,
) : State

package io.github.kei_1111.app.feature.splash.destination.splash

import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.common.result.successOrNull
import io.github.kei_1111.app.core.mvi.ViewModelState
import io.github.kei_1111.app.feature.splash.destination.splash.model.BuildStatus
import io.github.kei_1111.app.feature.splash.destination.splash.model.SplashStep
import io.github.kei_1111.shared.model.Works
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal data class SplashViewModelState(
    val jetBrainsMonoStep: SplashStep = SplashStep.Running,
    val notoSansJpStep: SplashStep = SplashStep.Running,
    val zenKakuGothicNewStep: SplashStep = SplashStep.Running,
    val renderStep: SplashStep = SplashStep.Running,
    val buildStatus: BuildStatus = BuildStatus.Running,
    val worksResult: Result<Works> = Result.Loading,
    override val effect: SplashEffect? = null,
) : ViewModelState<SplashState, SplashEffect> {
    /** 3種すべてのフォントがロード済みか。タイムアウト監視の停止判定と成功シーケンスの起点に使う。 */
    val allFontsLoaded: Boolean
        get() = jetBrainsMonoStep == SplashStep.Done &&
            notoSansJpStep == SplashStep.Done &&
            zenKakuGothicNewStep == SplashStep.Done

    override fun toState() = SplashState(
        jetBrainsMonoStep = jetBrainsMonoStep,
        notoSansJpStep = notoSansJpStep,
        zenKakuGothicNewStep = zenKakuGothicNewStep,
        renderStep = renderStep,
        buildStatus = buildStatus,
        isBuildFailed = buildStatus == BuildStatus.Failed,
        imagePrefetchUrls = imagePrefetchUrls(),
    )

    private fun imagePrefetchUrls(): ImmutableList<String> {
        val works = worksResult.successOrNull?.items ?: return persistentListOf()
        return (listOfNotNull(works.firstOrNull()?.screenshots?.firstOrNull()) + works.mapNotNull { it.iconUrl })
            .toImmutableList()
    }
}

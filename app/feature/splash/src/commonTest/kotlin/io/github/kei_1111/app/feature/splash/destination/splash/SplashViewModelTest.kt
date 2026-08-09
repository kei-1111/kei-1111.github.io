package io.github.kei_1111.app.feature.splash.destination.splash

import io.github.kei_1111.app.core.domain.usecase.GetContributionsUseCase
import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.app.core.testing.ViewModelTestBase
import io.github.kei_1111.app.core.testing.startCollecting
import io.github.kei_1111.app.feature.splash.destination.splash.model.BuildStatus
import io.github.kei_1111.app.feature.splash.destination.splash.model.SplashFont
import io.github.kei_1111.app.feature.splash.destination.splash.model.SplashStep
import io.github.kei_1111.app.feature.splash.destination.splash.theme.SplashAnimations
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val PARTIAL_VISIBLE_MILLIS = 6_000L

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest : ViewModelTestBase() {

    @Test
    fun marksFontStepDoneOnReceiveFontLoaded() = runTest {
        val viewModel = SplashViewModel(FakeGetProfileUseCase(), FakeGetContributionsUseCase())
        startCollecting(viewModel.state)

        viewModel.onIntent(SplashIntent.ReceiveFontLoaded(SplashFont.JetBrainsMono))
        runCurrent()

        assertEquals(SplashStep.Done, viewModel.state.value.jetBrainsMonoStep)
        assertEquals(SplashStep.Running, viewModel.state.value.notoSansJpStep)
        assertEquals(SplashStep.Running, viewModel.state.value.zenKakuGothicNewStep)
    }

    @Test
    fun completesSuccessSequenceAfterAllFontsLoaded() = runTest {
        val viewModel = SplashViewModel(FakeGetProfileUseCase(), FakeGetContributionsUseCase())
        startCollecting(viewModel.state)

        SplashFont.entries.forEach { font ->
            viewModel.onIntent(SplashIntent.ReceiveFontLoaded(font))
            runCurrent()
        }
        advanceTimeBy(SplashAnimations.MinDisplayMillis)
        runCurrent()

        assertEquals(SplashStep.Done, viewModel.state.value.jetBrainsMonoStep)
        assertEquals(SplashStep.Done, viewModel.state.value.notoSansJpStep)
        assertEquals(SplashStep.Done, viewModel.state.value.zenKakuGothicNewStep)
        assertEquals(SplashStep.Done, viewModel.state.value.renderStep)
        assertEquals(BuildStatus.Success, viewModel.state.value.buildStatus)

        advanceTimeBy(SplashAnimations.SuccessToExitMillis)
        runCurrent()

        assertEquals(SplashEffect.NavigateProfile, viewModel.state.value.effect)
    }

    @Test
    fun failsUnloadedStepsOnFontLoadTimeout() = runTest {
        val viewModel = SplashViewModel(FakeGetProfileUseCase(), FakeGetContributionsUseCase())
        startCollecting(viewModel.state)

        viewModel.onIntent(SplashIntent.ReceiveFontLoaded(SplashFont.JetBrainsMono))
        runCurrent()
        viewModel.onIntent(SplashIntent.UpdatePageVisibility(true))
        runCurrent()
        advanceTimeBy(SplashAnimations.FontLoadTimeoutMillis)
        runCurrent()

        assertEquals(SplashStep.Done, viewModel.state.value.jetBrainsMonoStep)
        assertEquals(SplashStep.Failed, viewModel.state.value.notoSansJpStep)
        assertEquals(SplashStep.Failed, viewModel.state.value.zenKakuGothicNewStep)
        assertEquals(SplashStep.Failed, viewModel.state.value.renderStep)
        assertEquals(BuildStatus.Failed, viewModel.state.value.buildStatus)

        advanceUntilIdle()

        assertNull(viewModel.state.value.effect)
    }

    @Test
    fun keepsRunningWhileHiddenPastTimeout() = runTest {
        val viewModel = SplashViewModel(FakeGetProfileUseCase(), FakeGetContributionsUseCase())
        startCollecting(viewModel.state)

        viewModel.onIntent(SplashIntent.UpdatePageVisibility(true))
        runCurrent()
        advanceTimeBy(PARTIAL_VISIBLE_MILLIS)
        runCurrent()
        viewModel.onIntent(SplashIntent.UpdatePageVisibility(false))
        runCurrent()
        advanceTimeBy(SplashAnimations.FontLoadTimeoutMillis * 2)
        runCurrent()

        assertEquals(BuildStatus.Running, viewModel.state.value.buildStatus)
    }

    @Test
    fun restartsTimeoutFromZeroOnReshow() = runTest {
        val viewModel = SplashViewModel(FakeGetProfileUseCase(), FakeGetContributionsUseCase())
        startCollecting(viewModel.state)

        viewModel.onIntent(SplashIntent.UpdatePageVisibility(true))
        runCurrent()
        advanceTimeBy(PARTIAL_VISIBLE_MILLIS)
        runCurrent()
        viewModel.onIntent(SplashIntent.UpdatePageVisibility(false))
        runCurrent()
        viewModel.onIntent(SplashIntent.UpdatePageVisibility(true))
        runCurrent()
        advanceTimeBy(SplashAnimations.FontLoadTimeoutMillis - 1)
        runCurrent()

        assertEquals(BuildStatus.Running, viewModel.state.value.buildStatus)

        advanceTimeBy(1)
        runCurrent()

        assertEquals(BuildStatus.Failed, viewModel.state.value.buildStatus)
    }

    @Test
    fun neverRestartsTimeoutOnceAllFontsDone() = runTest {
        val viewModel = SplashViewModel(FakeGetProfileUseCase(), FakeGetContributionsUseCase())
        startCollecting(viewModel.state)

        viewModel.onIntent(SplashIntent.UpdatePageVisibility(true))
        runCurrent()
        SplashFont.entries.forEach { font ->
            viewModel.onIntent(SplashIntent.ReceiveFontLoaded(font))
            runCurrent()
        }
        viewModel.onIntent(SplashIntent.UpdatePageVisibility(false))
        runCurrent()
        viewModel.onIntent(SplashIntent.UpdatePageVisibility(true))
        runCurrent()
        advanceUntilIdle()

        assertEquals(BuildStatus.Success, viewModel.state.value.buildStatus)
        assertEquals(SplashEffect.NavigateProfile, viewModel.state.value.effect)
    }

    @Test
    fun marksLateFontLoadedDoneWhileBuildStaysFailed() = runTest {
        val viewModel = SplashViewModel(FakeGetProfileUseCase(), FakeGetContributionsUseCase())
        startCollecting(viewModel.state)

        viewModel.onIntent(SplashIntent.UpdatePageVisibility(true))
        runCurrent()
        advanceTimeBy(SplashAnimations.FontLoadTimeoutMillis)
        runCurrent()

        viewModel.onIntent(SplashIntent.ReceiveFontLoaded(SplashFont.NotoSansJp))
        runCurrent()

        assertEquals(SplashStep.Done, viewModel.state.value.notoSansJpStep)
        assertEquals(SplashStep.Failed, viewModel.state.value.zenKakuGothicNewStep)
        assertEquals(SplashStep.Failed, viewModel.state.value.renderStep)
        assertEquals(BuildStatus.Failed, viewModel.state.value.buildStatus)

        advanceUntilIdle()

        assertNull(viewModel.state.value.effect)
    }

    @Test
    fun recoversToSuccessWhenAllFontsLoadAfterFailure() = runTest {
        val viewModel = SplashViewModel(FakeGetProfileUseCase(), FakeGetContributionsUseCase())
        startCollecting(viewModel.state)

        viewModel.onIntent(SplashIntent.UpdatePageVisibility(true))
        runCurrent()
        advanceTimeBy(SplashAnimations.FontLoadTimeoutMillis)
        runCurrent()
        SplashFont.entries.forEach { font ->
            viewModel.onIntent(SplashIntent.ReceiveFontLoaded(font))
            runCurrent()
        }
        advanceTimeBy(SplashAnimations.MinDisplayMillis)
        runCurrent()

        assertEquals(SplashStep.Done, viewModel.state.value.jetBrainsMonoStep)
        assertEquals(SplashStep.Done, viewModel.state.value.notoSansJpStep)
        assertEquals(SplashStep.Done, viewModel.state.value.zenKakuGothicNewStep)
        assertEquals(SplashStep.Done, viewModel.state.value.renderStep)
        assertEquals(BuildStatus.Success, viewModel.state.value.buildStatus)

        advanceTimeBy(SplashAnimations.SuccessToExitMillis)
        runCurrent()

        assertEquals(SplashEffect.NavigateProfile, viewModel.state.value.effect)
    }

    @Test
    fun clearsEffectOnConsumeEffect() = runTest {
        val viewModel = SplashViewModel(FakeGetProfileUseCase(), FakeGetContributionsUseCase())
        startCollecting(viewModel.state)

        SplashFont.entries.forEach { font ->
            viewModel.onIntent(SplashIntent.ReceiveFontLoaded(font))
            runCurrent()
        }
        advanceUntilIdle()

        viewModel.onIntent(SplashIntent.ConsumeEffect)
        runCurrent()

        assertNull(viewModel.state.value.effect)
    }
}

private class FakeGetProfileUseCase : GetProfileUseCase {
    private val profiles = MutableSharedFlow<GitHubProfile>()

    override fun invoke(): Flow<GitHubProfile> = profiles
}

private class FakeGetContributionsUseCase : GetContributionsUseCase {
    private val contributions = MutableSharedFlow<ContributionCalendar>()

    override fun invoke(): Flow<ContributionCalendar> = contributions
}

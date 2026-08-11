package io.github.kei_1111.app.feature.splash.destination.splash

import io.github.kei_1111.app.core.domain.usecase.GetContributionsUseCase
import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.app.core.domain.usecase.GetReadmeUseCase
import io.github.kei_1111.app.core.domain.usecase.GetWorksUseCase
import io.github.kei_1111.app.core.testing.ViewModelTestBase
import io.github.kei_1111.app.core.testing.startCollecting
import io.github.kei_1111.app.feature.splash.destination.splash.model.BuildStatus
import io.github.kei_1111.app.feature.splash.destination.splash.model.SplashFont
import io.github.kei_1111.app.feature.splash.destination.splash.model.SplashStep
import io.github.kei_1111.app.feature.splash.destination.splash.theme.SplashAnimations
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.Works
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
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
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            FakeGetWorksUseCase(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(SplashIntent.ReceiveFontLoaded(SplashFont.JetBrainsMono))
        runCurrent()

        assertEquals(SplashStep.Done, viewModel.state.value.jetBrainsMonoStep)
        assertEquals(SplashStep.Running, viewModel.state.value.notoSansJpStep)
        assertEquals(SplashStep.Running, viewModel.state.value.zenKakuGothicNewStep)
    }

    @Test
    fun completesSuccessSequenceAfterAllFontsLoaded() = runTest {
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            FakeGetWorksUseCase(),
        )
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
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            FakeGetWorksUseCase(),
        )
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
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            FakeGetWorksUseCase(),
        )
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
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            FakeGetWorksUseCase(),
        )
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
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            FakeGetWorksUseCase(),
        )
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
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            FakeGetWorksUseCase(),
        )
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
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            FakeGetWorksUseCase(),
        )
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
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            FakeGetWorksUseCase(),
        )
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

    @Test
    fun exposesNoPrefetchUrlsBeforeWorksArrive() = runTest {
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            FakeGetWorksUseCase(),
        )
        startCollecting(viewModel.state)
        runCurrent()

        assertEquals(persistentListOf(), viewModel.state.value.imagePrefetchUrls)
    }

    @Test
    fun exposesFirstScreenshotThenEveryWorkIconOnWorksLoaded() = runTest {
        val getWorksUseCase = FakeGetWorksUseCase()
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            getWorksUseCase,
        )
        startCollecting(viewModel.state)

        getWorksUseCase.emit(
            Works(
                items = persistentListOf(
                    work(id = "withmo", iconUrl = "images/works/withmo-icon.webp", screenshots = persistentListOf("a.webp", "b.webp")),
                    work(id = "portfolio", iconUrl = "images/works/portfolio-icon.webp", screenshots = persistentListOf("c.webp")),
                ),
            ),
        )
        runCurrent()

        assertEquals(
            persistentListOf("a.webp", "images/works/withmo-icon.webp", "images/works/portfolio-icon.webp"),
            viewModel.state.value.imagePrefetchUrls,
        )
    }

    @Test
    fun skipsMissingIconsAndScreenshotsWhenBuildingPrefetchUrls() = runTest {
        val getWorksUseCase = FakeGetWorksUseCase()
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            getWorksUseCase,
        )
        startCollecting(viewModel.state)

        getWorksUseCase.emit(
            Works(
                items = persistentListOf(
                    work(id = "no-shots", iconUrl = null, screenshots = persistentListOf()),
                    work(id = "iconless", iconUrl = null, screenshots = persistentListOf("c.webp")),
                    work(id = "full", iconUrl = "icon.webp", screenshots = persistentListOf("d.webp")),
                ),
            ),
        )
        runCurrent()

        assertEquals(persistentListOf("icon.webp"), viewModel.state.value.imagePrefetchUrls)
    }

    @Test
    fun keepsPrefetchUrlsEmptyWhenWorksFetchFails() = runTest {
        val getWorksUseCase = FakeGetWorksUseCase()
        val viewModel = SplashViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetReadmeUseCase(),
            getWorksUseCase,
        )
        startCollecting(viewModel.state)

        getWorksUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertEquals(persistentListOf(), viewModel.state.value.imagePrefetchUrls)
    }
}

private fun work(
    id: String,
    iconUrl: String?,
    screenshots: ImmutableList<String>,
) = Work(
    id = id,
    name = id,
    kind = "",
    period = "",
    description = LocalizedText(ja = "", en = ""),
    iconUrl = iconUrl,
    screenshots = screenshots,
)

private class FakeGetWorksUseCase : GetWorksUseCase {
    private val results = MutableSharedFlow<Result<Works>>(replay = 1)

    override fun invoke(): Flow<Works> = results.map { it.getOrThrow() }

    suspend fun emit(works: Works) = results.emit(Result.success(works))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

private class FakeGetProfileUseCase : GetProfileUseCase {
    private val profiles = MutableSharedFlow<GitHubProfile>()

    override fun invoke(): Flow<GitHubProfile> = profiles
}

private class FakeGetContributionsUseCase : GetContributionsUseCase {
    private val contributions = MutableSharedFlow<ContributionCalendar>()

    override fun invoke(): Flow<ContributionCalendar> = contributions
}

private class FakeGetReadmeUseCase : GetReadmeUseCase {
    private val readmes = MutableSharedFlow<Readme>()

    override fun invoke(): Flow<Readme> = readmes
}

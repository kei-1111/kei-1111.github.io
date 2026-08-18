package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import io.github.kei_1111.app.core.common.logging.InteractionLog
import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.app.core.testing.ViewModelTestBase
import io.github.kei_1111.app.core.testing.dispatch
import io.github.kei_1111.app.core.testing.startCollecting
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereEntry
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import io.github.kei_1111.app.feature.profile.fake.FakeGetProfileUseCase
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Profile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchEverywhereViewModelTest : ViewModelTestBase() {

    @Test
    fun includesLinkEntriesOnceProfileLoads() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)

        fakeGetProfileUseCase.emit(testProfile(links = persistentListOf(gitHubLink)))
        runCurrent()

        assertTrue(
            viewModel.state.value.results.any { it is SearchEverywhereEntry.Link && it.name == gitHubLink.name },
        )
    }

    @Test
    fun ranksNameMatchesFirstInFilteredResults() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)
        fakeGetProfileUseCase.emit(testProfile(links = persistentListOf(gitHubLink)))
        runCurrent()

        dispatch(viewModel, SearchEverywhereIntent.UpdateQuery("github"))

        // 挿入順ではブレッドクラムに io.github を含む Page が先行するため、
        // name マッチ(2倍重み)の Link が先頭に来ることがランキングの証明になる
        val results = viewModel.state.value.results
        assertTrue(results.any { it is SearchEverywhereEntry.Page })
        assertTrue(results.first() is SearchEverywhereEntry.Link)
        assertEquals(gitHubLink.name, results.first().name)
    }

    @Test
    fun resetsSelectionToTopOnQueryUpdate() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, SearchEverywhereIntent.MoveSelection(2))

        dispatch(viewModel, SearchEverywhereIntent.UpdateQuery("README"))

        assertEquals(0, viewModel.state.value.selectedIndex)
    }

    @Test
    fun cyclesTabBackwardWrappingToLast() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, SearchEverywhereIntent.CycleTab(-1))

        assertEquals(SearchEverywhereTab.Actions, viewModel.state.value.selectedTab)
    }

    @Test
    fun clampsSelectionToLastResult() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        val lastIndex = viewModel.state.value.results.lastIndex
        dispatch(viewModel, SearchEverywhereIntent.MoveSelection(lastIndex + 5))

        assertEquals(lastIndex, viewModel.state.value.selectedIndex)
    }

    @Test
    fun clearsEffectOnConsumeEffect() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, SearchEverywhereIntent.OpenEntry(SearchEverywhereEntry.SwitchTheme))

        dispatch(viewModel, SearchEverywhereIntent.ConsumeEffect)

        assertNull(viewModel.state.value.effect)
    }
}

private fun createViewModel(
    getProfileUseCase: GetProfileUseCase = FakeGetProfileUseCase(),
    interactionLog: InteractionLog = InteractionLog(),
) = SearchEverywhereViewModel(
    getProfileUseCase,
    interactionLog,
)

private val gitHubLink = LinkService(
    type = LinkServiceType.GitHub,
    name = "GitHub",
    url = "https://github.com/kei-1111",
)

private fun testProfile(
    links: ImmutableList<LinkService> = persistentListOf(),
) = Profile(
    name = LocalizedText(ja = "ケイ", en = "Kei"),
    handle = "kei-1111",
    location = "Tokyo",
    role = "Student",
    followers = 0,
    following = 0,
    repos = 0,
    totalStars = 0,
    pinnedRepos = persistentListOf(),
    languages = persistentListOf(),
    links = links,
)

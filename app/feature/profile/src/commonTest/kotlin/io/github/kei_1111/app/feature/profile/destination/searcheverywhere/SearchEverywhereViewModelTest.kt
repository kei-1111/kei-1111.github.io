package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import io.github.kei_1111.app.core.common.logging.InteractionLog
import io.github.kei_1111.app.core.testing.ViewModelTestBase
import io.github.kei_1111.app.core.testing.startCollecting
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereEntry
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.toEffect
import io.github.kei_1111.app.feature.profile.fake.FakeGetProfileUseCase
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
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
        val viewModel = SearchEverywhereViewModel(fakeGetProfileUseCase, InteractionLog())
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
        val viewModel = SearchEverywhereViewModel(fakeGetProfileUseCase, InteractionLog())
        startCollecting(viewModel.state)
        fakeGetProfileUseCase.emit(testProfile(links = persistentListOf(gitHubLink)))
        runCurrent()

        viewModel.onIntent(SearchEverywhereIntent.UpdateQuery("github"))
        runCurrent()

        // 挿入順ではブレッドクラムに io.github を含む Page が先行するため、
        // name マッチ(2倍重み)の Link が先頭に来ることがランキングの証明になる
        val results = viewModel.state.value.results
        assertTrue(results.any { it is SearchEverywhereEntry.Page })
        assertTrue(results.first() is SearchEverywhereEntry.Link)
        assertEquals(gitHubLink.name, results.first().name)
    }

    @Test
    fun resetsSelectionToTopOnQueryUpdate() = runTest {
        val viewModel = SearchEverywhereViewModel(FakeGetProfileUseCase(), InteractionLog())
        startCollecting(viewModel.state)
        viewModel.onIntent(SearchEverywhereIntent.MoveSelection(2))
        runCurrent()

        viewModel.onIntent(SearchEverywhereIntent.UpdateQuery("README"))
        runCurrent()

        assertEquals(0, viewModel.state.value.selectedIndex)
    }

    @Test
    fun cyclesTabBackwardWrappingToLast() = runTest {
        val viewModel = SearchEverywhereViewModel(FakeGetProfileUseCase(), InteractionLog())
        startCollecting(viewModel.state)

        viewModel.onIntent(SearchEverywhereIntent.CycleTab(-1))
        runCurrent()

        assertEquals(SearchEverywhereTab.Actions, viewModel.state.value.selectedTab)
    }

    @Test
    fun clampsSelectionToLastResult() = runTest {
        val viewModel = SearchEverywhereViewModel(FakeGetProfileUseCase(), InteractionLog())
        startCollecting(viewModel.state)

        val lastIndex = viewModel.state.value.results.lastIndex
        viewModel.onIntent(SearchEverywhereIntent.MoveSelection(lastIndex + 5))
        runCurrent()

        assertEquals(lastIndex, viewModel.state.value.selectedIndex)
    }

    @Test
    fun opensTheHighlightedEntryOnOpenSelectedEntry() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = SearchEverywhereViewModel(fakeGetProfileUseCase, InteractionLog())
        startCollecting(viewModel.state)
        fakeGetProfileUseCase.emit(testProfile(links = persistentListOf(gitHubLink)))
        runCurrent()
        viewModel.onIntent(SearchEverywhereIntent.MoveSelection(1))
        runCurrent()

        val highlighted = viewModel.state.value.results[viewModel.state.value.selectedIndex]
        viewModel.onIntent(SearchEverywhereIntent.OpenSelectedEntry)
        runCurrent()

        // 表示中のハイライト行と Enter で開く対象が同じ導出であることを固定する
        assertEquals(highlighted.toEffect(), viewModel.state.value.effect)
    }

    @Test
    fun keepsTheSelectionInsideTheTabResultsOnOpenSelectedEntry() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = SearchEverywhereViewModel(fakeGetProfileUseCase, InteractionLog())
        startCollecting(viewModel.state)
        fakeGetProfileUseCase.emit(testProfile(links = persistentListOf(gitHubLink)))
        runCurrent()
        viewModel.onIntent(SearchEverywhereIntent.MoveSelection(Int.MAX_VALUE))
        runCurrent()

        viewModel.onIntent(SearchEverywhereIntent.UpdateSelectedTab(SearchEverywhereTab.Links))
        runCurrent()
        viewModel.onIntent(SearchEverywhereIntent.OpenSelectedEntry)
        runCurrent()

        assertEquals(SearchEverywhereEffect.OpenUrl(gitHubLink.url), viewModel.state.value.effect)
    }

    @Test
    fun narrowsResultsToTheSelectedTab() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = SearchEverywhereViewModel(fakeGetProfileUseCase, InteractionLog())
        startCollecting(viewModel.state)
        fakeGetProfileUseCase.emit(testProfile(links = persistentListOf(gitHubLink)))
        runCurrent()

        viewModel.onIntent(SearchEverywhereIntent.UpdateSelectedTab(SearchEverywhereTab.Links))
        runCurrent()

        val results = viewModel.state.value.results
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it is SearchEverywhereEntry.Link })
        assertEquals(0, viewModel.state.value.selectedIndex)
    }

    @Test
    fun clearsEffectOnConsumeEffect() = runTest {
        val viewModel = SearchEverywhereViewModel(FakeGetProfileUseCase(), InteractionLog())
        startCollecting(viewModel.state)
        viewModel.onIntent(SearchEverywhereIntent.OpenEntry(SearchEverywhereEntry.SwitchTheme))
        runCurrent()

        viewModel.onIntent(SearchEverywhereIntent.ConsumeEffect)
        runCurrent()

        assertNull(viewModel.state.value.effect)
    }
}

private val gitHubLink = LinkService(
    type = LinkServiceType.GitHub,
    name = "GitHub",
    url = "https://github.com/kei-1111",
)

private fun testProfile(
    links: ImmutableList<LinkService> = persistentListOf(),
) = GitHubProfile(
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

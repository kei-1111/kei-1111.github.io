package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.mvi.ViewModelState
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereEntry
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.searchEntries
import io.github.kei_1111.shared.model.GitHubProfile
import kotlinx.collections.immutable.ImmutableList

internal data class SearchEverywhereViewModelState(
    val query: String = "",
    val selectedTab: SearchEverywhereTab = SearchEverywhereTab.All,
    val selectedIndex: Int = 0,
    val profileResult: Result<GitHubProfile> = Result.Loading,
    val effect: SearchEverywhereEffect? = null,
) : ViewModelState<SearchEverywhereState> {
    fun results(): ImmutableList<SearchEverywhereEntry> {
        val links = (profileResult as? Result.Success<GitHubProfile>)?.data?.links.orEmpty()
        return searchEntries(query, selectedTab, links)
    }

    /** [index] を [results] の範囲に収める。0 件でも例外にならないよう下限 0 で丸める。 */
    fun clampToResults(index: Int, results: List<*>): Int = index.coerceIn(0, results.lastIndex.coerceAtLeast(0))

    /** 画面がハイライトしている行と同じエントリ。Enter で開く対象を表示と一致させるために使う。 */
    fun selectedEntry(): SearchEverywhereEntry? {
        val results = results()
        return results.getOrNull(clampToResults(selectedIndex, results))
    }

    override fun toState(): SearchEverywhereState {
        val results = results()
        return SearchEverywhereState(
            query = query,
            selectedTab = selectedTab,
            results = results,
            selectedIndex = clampToResults(selectedIndex, results),
            effect = effect,
        )
    }
}

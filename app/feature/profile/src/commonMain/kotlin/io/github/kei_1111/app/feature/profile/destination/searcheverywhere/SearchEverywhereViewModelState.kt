package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.common.result.successOrNull
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
    /** 表示・選択クランプ・Enter 対象が共有する検索結果の導出。片側だけ変えると表示と実行対象がずれる。 */
    fun searchResults(): ImmutableList<SearchEverywhereEntry> =
        searchEntries(query, selectedTab, profileResult.successOrNull?.links.orEmpty())

    /** 0 件でも例外にならないよう下限 0 で丸める。 */
    fun clampToResults(index: Int, results: List<SearchEverywhereEntry>): Int =
        index.coerceIn(0, results.lastIndex.coerceAtLeast(0))

    override fun toState(): SearchEverywhereState {
        val results = searchResults()
        return SearchEverywhereState(
            query = query,
            selectedTab = selectedTab,
            results = results,
            selectedIndex = clampToResults(selectedIndex, results),
            effect = effect,
        )
    }
}

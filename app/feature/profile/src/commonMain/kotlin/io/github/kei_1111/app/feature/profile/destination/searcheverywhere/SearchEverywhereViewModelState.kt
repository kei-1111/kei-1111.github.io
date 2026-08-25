package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.common.result.successOrNull
import io.github.kei_1111.app.core.mvi.ViewModelState
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereEntry
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.searchEntries
import io.github.kei_1111.shared.model.Profile
import kotlinx.collections.immutable.ImmutableList

internal data class SearchEverywhereViewModelState(
    val query: String = "",
    val selectedTab: SearchEverywhereTab = SearchEverywhereTab.All,
    val selectedIndex: Int = 0,
    val profileResult: Result<Profile> = Result.Loading,
    override val effect: SearchEverywhereEffect? = null,
) : ViewModelState<SearchEverywhereState, SearchEverywhereEffect> {
    /** 表示・選択クランプ・Enter 対象が共有する検索結果の導出。片側だけ変えると表示と実行対象がずれる。 */
    fun results(): ImmutableList<SearchEverywhereEntry> {
        val links = profileResult.successOrNull?.links.orEmpty()
        return searchEntries(query, selectedTab, links)
    }

    /** 0 件でも例外にならないよう下限 0 で丸める。 */
    fun clampToResults(index: Int, results: List<SearchEverywhereEntry>): Int =
        index.coerceIn(0, results.lastIndex.coerceAtLeast(0))

    /** ハイライト中の行。フッターの表示と Enter で開く対象が同じ導出を共有する。 */
    fun selectedEntry(): SearchEverywhereEntry? {
        val results = results()
        return results.getOrNull(clampToResults(selectedIndex, results))
    }

    override fun toState(): SearchEverywhereState {
        val results = results()
        val selected = results.getOrNull(clampToResults(selectedIndex, results))
        return SearchEverywhereState(
            query = query,
            selectedTab = selectedTab,
            results = results,
            selectedIndex = clampToResults(selectedIndex, results),
            // 詳細を持たないエントリ（Switch Theme）は名前で埋める
            selectedEntryDetail = selected?.let { it.detail.ifEmpty { it.name } },
        )
    }
}

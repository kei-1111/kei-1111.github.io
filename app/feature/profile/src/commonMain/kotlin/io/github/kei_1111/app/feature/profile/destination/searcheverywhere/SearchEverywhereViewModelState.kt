package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.mvi.ViewModelState
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereEntry
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.fuzzyScore
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.GitHubProfile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal data class SearchEverywhereViewModelState(
    val query: String = "",
    val selectedTab: SearchEverywhereTab = SearchEverywhereTab.All,
    val selectedIndex: Int = 0,
    val profileResult: Result<GitHubProfile> = Result.Loading,
    val effect: SearchEverywhereEffect? = null,
) : ViewModelState<SearchEverywhereState> {
    fun results(): ImmutableList<SearchEverywhereEntry> {
        val pages = EditorPage.entries.map(SearchEverywhereEntry::Page)
        val links = (profileResult as? Result.Success<GitHubProfile>)?.data?.links.orEmpty().map(SearchEverywhereEntry::Link)
        val actions = listOf(SearchEverywhereEntry.SwitchTheme)
        val entries = when (selectedTab) {
            SearchEverywhereTab.All -> pages + links + actions
            SearchEverywhereTab.Files -> pages
            SearchEverywhereTab.Links -> links
            SearchEverywhereTab.Actions -> actions
        }
        if (query.isBlank()) return entries.toImmutableList()

        return entries.mapIndexedNotNull { index, entry ->
            val nameScore = fuzzyScore(query, entry.name)?.times(2)
            val detailScore = fuzzyScore(query, entry.detail)
            val score = listOfNotNull(nameScore, detailScore).maxOrNull()
            score?.let { ScoredEntry(entry = entry, score = it, index = index) }
        }.sortedWith(compareByDescending<ScoredEntry> { it.score }.thenBy { it.index })
            .map { it.entry }
            .toImmutableList()
    }

    override fun toState(): SearchEverywhereState {
        val results = results()
        return SearchEverywhereState(
            query = query,
            selectedTab = selectedTab,
            results = results,
            selectedIndex = selectedIndex.coerceIn(0, results.lastIndex.coerceAtLeast(0)),
            effect = effect,
        )
    }
}

private data class ScoredEntry(
    val entry: SearchEverywhereEntry,
    val score: Int,
    val index: Int,
)

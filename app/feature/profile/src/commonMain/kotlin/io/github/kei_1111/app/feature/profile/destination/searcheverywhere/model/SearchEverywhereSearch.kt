package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model

import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.LinkService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal fun searchEntries(
    query: String,
    tab: SearchEverywhereTab,
    links: List<LinkService>,
): ImmutableList<SearchEverywhereEntry> {
    val pages = EditorPage.entries.map(SearchEverywhereEntry::Page)
    val linkEntries = links.map(SearchEverywhereEntry::Link)
    val actions = listOf(SearchEverywhereEntry.SwitchTheme)
    val entries = when (tab) {
        SearchEverywhereTab.All -> pages + linkEntries + actions
        SearchEverywhereTab.Files -> pages
        SearchEverywhereTab.Links -> linkEntries
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

private data class ScoredEntry(
    val entry: SearchEverywhereEntry,
    val score: Int,
    val index: Int,
)

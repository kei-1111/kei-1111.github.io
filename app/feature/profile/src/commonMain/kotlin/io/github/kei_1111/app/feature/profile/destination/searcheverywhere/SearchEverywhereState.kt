package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import io.github.kei_1111.app.core.mvi.State
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereEntry
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class SearchEverywhereState(
    val query: String = "",
    val selectedTab: SearchEverywhereTab = SearchEverywhereTab.All,
    val results: ImmutableList<SearchEverywhereEntry> = persistentListOf(),
    val selectedIndex: Int = 0,
    /** フッターに出すハイライト行の説明。null = 結果なし。 */
    val selectedEntryDetail: String? = null,
    val effect: SearchEverywhereEffect? = null,
) : State

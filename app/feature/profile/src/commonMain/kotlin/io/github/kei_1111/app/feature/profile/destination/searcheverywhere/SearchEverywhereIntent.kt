package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import io.github.kei_1111.app.core.mvi.Intent
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereEntry
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab

internal sealed interface SearchEverywhereIntent : Intent {
    data class UpdateQuery(val query: String) : SearchEverywhereIntent
    data class UpdateSelectedTab(val tab: SearchEverywhereTab) : SearchEverywhereIntent
    data class MoveSelection(val delta: Int) : SearchEverywhereIntent
    data class OpenEntry(val entry: SearchEverywhereEntry) : SearchEverywhereIntent
    data object OpenSelectedEntry : SearchEverywhereIntent
    data object Dismiss : SearchEverywhereIntent
    data object ConsumeEffect : SearchEverywhereIntent
}

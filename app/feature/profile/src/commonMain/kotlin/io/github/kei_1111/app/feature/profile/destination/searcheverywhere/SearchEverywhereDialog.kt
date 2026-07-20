@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.component.SearchField
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.component.SearchFooter
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.component.SearchHeader
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.component.SearchResults
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.preview.PreviewSearchEntries
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.theme.SearchEverywhereDimensions

@Composable
internal fun SearchEverywhereDialog(
    state: SearchEverywhereState,
    onIntent: (SearchEverywhereIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val panelWidth = (maxWidth - SearchEverywhereDimensions.PanelHorizontalMargin)
            .coerceAtMost(SearchEverywhereDimensions.PanelMaxWidth)
        val panelHeight = (maxHeight * SearchEverywhereDimensions.PanelHeightFraction)
            .coerceAtMost(SearchEverywhereDimensions.PanelMaxHeight)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = maxHeight * SearchEverywhereDimensions.PanelTopFraction)
                .width(panelWidth)
                .height(panelHeight)
                .clip(KeiTheme.shapes.island)
                .background(KeiTheme.colors.island)
                .border(
                    width = SearchEverywhereDimensions.PanelBorderWidth,
                    color = KeiTheme.colors.outline,
                    shape = KeiTheme.shapes.island,
                ),
        ) {
            SearchHeader(
                selectedTab = state.selectedTab,
                onClickTab = { onIntent(SearchEverywhereIntent.UpdateSelectedTab(it)) },
                compact = panelWidth < SearchEverywhereDimensions.PanelCompactWidth,
            )
            SearchField(
                query = state.query,
                onChangeQuery = { onIntent(SearchEverywhereIntent.UpdateQuery(it)) },
                onMoveSelection = { onIntent(SearchEverywhereIntent.MoveSelection(it)) },
                onOpenSelected = { onIntent(SearchEverywhereIntent.OpenSelectedEntry) },
                onDismiss = { onIntent(SearchEverywhereIntent.Dismiss) },
                onCycleTab = { delta ->
                    val tabs = SearchEverywhereTab.entries
                    val selectedIndex = tabs.indexOf(state.selectedTab)
                    val nextIndex = (selectedIndex + delta + tabs.size) % tabs.size
                    onIntent(SearchEverywhereIntent.UpdateSelectedTab(tabs[nextIndex]))
                },
            )
            SearchResults(
                results = state.results,
                selectedIndex = state.selectedIndex,
                onClickEntry = { onIntent(SearchEverywhereIntent.OpenEntry(it)) },
                modifier = Modifier.weight(1f),
            )
            val selectedEntry = state.results.getOrNull(state.selectedIndex)
            SearchFooter(detail = selectedEntry?.detail?.ifEmpty { selectedEntry.name })
        }
    }
}

@Preview
@Composable
private fun SearchEverywhereDialogPreview() {
    KeiTheme {
        Box(modifier = Modifier.size(width = 960.dp, height = 720.dp).background(KeiTheme.colors.desk)) {
            SearchEverywhereDialog(
                state = SearchEverywhereState(results = PreviewSearchEntries),
                onIntent = {},
            )
        }
    }
}

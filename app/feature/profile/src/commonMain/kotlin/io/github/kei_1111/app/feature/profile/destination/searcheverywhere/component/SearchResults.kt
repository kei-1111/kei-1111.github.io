@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.KeiThemeController
import io.github.kei_1111.app.core.designsystem.theme.brandColor
import io.github.kei_1111.app.core.designsystem.theme.icon
import io.github.kei_1111.app.core.ui.rememberHoverState
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereEntry
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.preview.PreviewSearchEntries
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.theme.SearchEverywhereDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SearchResults(
    results: ImmutableList<SearchEverywhereEntry>,
    selectedIndex: Int,
    onClickEntry: (SearchEverywhereEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (results.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.TopCenter) {
            Text(
                text = "Nothing found",
                style = KeiTheme.typography.chrome.copy(
                    fontSize = SearchEverywhereDimensions.NameFontSize,
                    color = KeiTheme.colors.textSecondary,
                ),
            )
        }
        return
    }

    val lazyListState = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        lazyListState.scrollToItem(selectedIndex.coerceIn(results.indices))
    }
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
    ) {
        items(results.size) { index ->
            val entry = results[index]
            SearchResultRow(
                entry = entry,
                selected = index == selectedIndex,
                onClick = { onClickEntry(entry) },
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    entry: SearchEverywhereEntry,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverState = rememberHoverState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SearchEverywhereDimensions.RowHeight)
            .clip(KeiTheme.shapes.row)
            // 実 AS の Search Everywhere は選択行だけ青ピル（ツリー行のグレーではない）。
            .background(
                when {
                    selected -> KeiTheme.colors.tabSelected
                    hoverState.hovered -> KeiTheme.colors.chip
                    else -> Color.Transparent
                },
            )
            .hoverable(hoverState.interactionSource)
            .clickable(interactionSource = hoverState.interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EntryIcon(entry = entry)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = entry.name,
            style = KeiTheme.typography.chrome.copy(
                fontSize = SearchEverywhereDimensions.NameFontSize,
                color = KeiTheme.colors.textPrimary,
            ),
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = entry.detail,
            modifier = Modifier.weight(1f),
            style = KeiTheme.typography.chrome.copy(
                fontSize = SearchEverywhereDimensions.DetailFontSize,
                color = KeiTheme.colors.textSecondary,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = entry.categoryLabel,
            modifier = Modifier.padding(start = 8.dp),
            style = KeiTheme.typography.chrome.copy(
                fontSize = SearchEverywhereDimensions.CategoryFontSize,
                color = KeiTheme.colors.textSecondary,
            ),
        )
    }
}

@Composable
private fun EntryIcon(
    entry: SearchEverywhereEntry,
    modifier: Modifier = Modifier,
) {
    when (entry) {
        is SearchEverywhereEntry.Page -> KeiIcon(
            icon = if (entry.page == EditorPage.Readme) KeiTheme.icons.markdown else KeiTheme.icons.kotlin,
            contentDescription = null,
            modifier = modifier.size(SearchEverywhereDimensions.IconSize),
        )

        is SearchEverywhereEntry.Link -> Icon(
            painter = painterResource(entry.service.type.icon),
            contentDescription = null,
            modifier = modifier.size(SearchEverywhereDimensions.IconSize),
            tint = entry.service.type.brandColor,
        )

        SearchEverywhereEntry.SwitchTheme -> KeiIcon(
            icon = if (KeiThemeController.isDark) KeiTheme.icons.themeLight else KeiTheme.icons.themeDark,
            contentDescription = null,
            tint = KeiTheme.colors.mutedHigh,
            modifier = modifier.size(SearchEverywhereDimensions.IconSize),
        )
    }
}

@Preview
@Composable
private fun SearchResultsPreview() {
    KeiTheme {
        Box(modifier = Modifier.size(width = 700.dp, height = 240.dp).background(KeiTheme.colors.island)) {
            SearchResults(
                results = PreviewSearchEntries,
                selectedIndex = 0,
                onClickEntry = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.TintedIcon
import io.github.kei_1111.app.core.ui.rememberHoverState
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.theme.SearchEverywhereDimensions

@Composable
internal fun SearchHeader(
    selectedTab: SearchEverywhereTab,
    onClickTab: (SearchEverywhereTab) -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SearchEverywhereDimensions.HeaderHeight)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            SearchEverywhereTab.entries.forEach { tab ->
                SearchTabChip(
                    tab = tab,
                    selected = tab == selectedTab,
                    onClick = { onClickTab(tab) },
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (!compact) {
            IncludeNonProjectItems()
        }
    }
}

@Composable
private fun SearchTabChip(
    tab: SearchEverywhereTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverState = rememberHoverState()
    Text(
        text = tab.label,
        modifier = modifier
            .clip(KeiTheme.shapes.pill)
            .background(
                when {
                    selected -> KeiTheme.colors.selectionPill
                    hoverState.hovered -> KeiTheme.colors.chip
                    else -> Color.Transparent
                },
            )
            .hoverable(hoverState.interactionSource)
            .clickable(interactionSource = hoverState.interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        style = KeiTheme.typography.chrome.copy(
            fontSize = SearchEverywhereDimensions.TabFontSize,
            color = if (selected) KeiTheme.colors.textPrimary else KeiTheme.colors.textSecondary,
        ),
    )
}

@Composable
private fun IncludeNonProjectItems(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(SearchEverywhereDimensions.CheckboxSize)
                .border(1.dp, KeiTheme.colors.mutedHigh, KeiTheme.shapes.badge),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = "Include non-project items",
            style = KeiTheme.typography.chrome.copy(
                fontSize = SearchEverywhereDimensions.TabFontSize,
                color = KeiTheme.colors.textSecondary,
            ),
        )
        Spacer(modifier = Modifier.size(10.dp))
        DecorativeIcon(icon = KeiTheme.icons.openInToolWindow)
        Spacer(modifier = Modifier.size(8.dp))
        DecorativeIcon(icon = KeiTheme.icons.filter)
        Spacer(modifier = Modifier.size(8.dp))
        DecorativeIcon(icon = KeiTheme.icons.pin)
    }
}

@Composable
private fun DecorativeIcon(
    icon: TintedIcon,
    modifier: Modifier = Modifier,
) {
    KeiIcon(
        icon = icon,
        contentDescription = null,
        tint = KeiTheme.colors.mutedHigh,
        modifier = modifier.size(SearchEverywhereDimensions.IconSize),
    )
}

@Preview
@Composable
private fun SearchHeaderPreview() {
    KeiTheme {
        Box(modifier = Modifier.size(width = 700.dp, height = 40.dp).background(KeiTheme.colors.island)) {
            SearchHeader(selectedTab = SearchEverywhereTab.All, onClickTab = {}, compact = false)
        }
    }
}

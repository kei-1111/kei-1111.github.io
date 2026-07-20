@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.theme.SearchEverywhereDimensions

@Composable
internal fun SearchFooter(
    detail: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SearchEverywhereDimensions.DividerHeight)
                .background(KeiTheme.colors.popupBorder),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SearchEverywhereDimensions.FooterHeight)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = detail.orEmpty(),
                modifier = Modifier.weight(1f),
                style = KeiTheme.typography.chrome.copy(
                    fontSize = SearchEverywhereDimensions.CategoryFontSize,
                    color = KeiTheme.colors.textSecondary,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 実 AS の右下に出るショートカット表示。ここでは分割ペインを持たないため飾りとして置く。
            Text(
                text = "Open In Right Split",
                modifier = Modifier.padding(start = 12.dp),
                style = KeiTheme.typography.chrome.copy(
                    fontSize = SearchEverywhereDimensions.CategoryFontSize,
                    color = KeiTheme.colors.syntaxLink,
                ),
                maxLines = 1,
            )
        }
    }
}

@Preview
@Composable
private fun SearchFooterPreview() {
    KeiTheme {
        Box(modifier = Modifier.fillMaxWidth().background(KeiTheme.colors.popup)) {
            SearchFooter(detail = "app › src › main › kotlin › ProfileScreen.kt")
        }
    }
}

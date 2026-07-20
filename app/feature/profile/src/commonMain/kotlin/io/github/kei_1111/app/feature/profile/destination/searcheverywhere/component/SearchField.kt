@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.theme.SearchEverywhereDimensions

@Composable
internal fun SearchField(
    query: String,
    onChangeQuery: (String) -> Unit,
    onMoveSelection: (Int) -> Unit,
    onOpenSelected: () -> Unit,
    onDismiss: () -> Unit,
    onCycleTab: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(top = 6.dp, bottom = 8.dp)
            .height(SearchEverywhereDimensions.FieldHeight)
            .clip(KeiTheme.shapes.row)
            // 実 AS は常時フォーカス済みで、塗りはポップアップ面のまま枠線だけがブランド青になる。
            .border(
                width = SearchEverywhereDimensions.FieldBorderWidth,
                color = KeiTheme.colors.focusBorder,
                shape = KeiTheme.shapes.row,
            )
            .background(KeiTheme.colors.popup)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.search,
            contentDescription = null,
            tint = KeiTheme.colors.mutedHigh,
            modifier = Modifier.size(SearchEverywhereDimensions.IconSize),
        )
        Spacer(modifier = Modifier.size(6.dp))
        BasicTextField(
            value = query,
            onValueChange = onChangeQuery,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) {
                        false
                    } else {
                        when (event.key) {
                            Key.DirectionDown -> {
                                onMoveSelection(1)
                                true
                            }

                            Key.DirectionUp -> {
                                onMoveSelection(-1)
                                true
                            }

                            Key.Enter, Key.NumPadEnter -> {
                                onOpenSelected()
                                true
                            }

                            Key.Escape -> {
                                onDismiss()
                                true
                            }

                            Key.Tab -> {
                                onCycleTab(if (event.isShiftPressed) -1 else 1)
                                true
                            }

                            else -> false
                        }
                    }
                },
            singleLine = true,
            textStyle = KeiTheme.typography.chrome.copy(
                fontSize = SearchEverywhereDimensions.NameFontSize,
                color = KeiTheme.colors.textPrimary,
            ),
            cursorBrush = SolidColor(KeiTheme.colors.textPrimary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    innerTextField()
                    if (query.isEmpty()) {
                        Text(
                            text = "Type / to see commands",
                            modifier = Modifier.align(Alignment.CenterEnd),
                            style = KeiTheme.typography.chrome.copy(
                                fontSize = SearchEverywhereDimensions.DetailFontSize,
                                color = KeiTheme.colors.muted,
                            ),
                        )
                    }
                }
            },
        )
    }
}

@Preview
@Composable
private fun SearchFieldPreview() {
    KeiTheme {
        Box(modifier = Modifier.size(width = 700.dp, height = 54.dp).background(KeiTheme.colors.popup)) {
            SearchField(
                query = "",
                onChangeQuery = {},
                onMoveSelection = {},
                onOpenSelected = {},
                onDismiss = {},
                onCycleTab = {},
            )
        }
    }
}

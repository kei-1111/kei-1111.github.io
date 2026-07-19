@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions

/**
 * ステータスバー。デスク上に直接。左にパンくず、右に位置/改行/エンコード情報。
 * 全タブを閉じている間はパンくずを空にする。
 * デスクからの余白は親が設定する。
 */
@Composable
internal fun StatusBar(
    page: EditorPage?,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Breadcrumb(page = page, modifier = Modifier.weight(1f))
        StatusItems(readOnly = readOnly)
    }
}

@Composable
private fun Breadcrumb(
    page: EditorPage?,
    modifier: Modifier = Modifier,
) {
    Text(
        text = page?.breadcrumb ?: "",
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(fontSize = ProfileDimensions.ChromeLabelFontSize, color = KeiTheme.colors.textSecondary),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** ステータスバー右の位置/改行/エンコード情報 + インスペクション状態。 */
@Composable
private fun StatusItems(
    readOnly: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusItem("1:1")
        StatusItem("LF")
        StatusItem("UTF-8")
        StatusItem("4 spaces")
        if (readOnly) {
            KeiIcon(
                icon = KeiTheme.icons.lock,
                contentDescription = "読み取り専用",
                tint = KeiTheme.colors.mutedHigh,
                modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
            )
        }
        InspectionsIndicator()
    }
}

@Composable
private fun StatusItem(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = KeiTheme.colors.textSecondary,
) {
    Text(
        text = text,
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(fontSize = ProfileDimensions.ChromeLabelFontSize, color = color),
    )
}

@Composable
private fun InspectionsIndicator(modifier: Modifier = Modifier) {
    KeiIcon(
        icon = KeiTheme.icons.inspectionsOk,
        contentDescription = null,
        modifier = modifier.size(ProfileDimensions.ChromeIconSize),
    )
}

@Preview
@Composable
private fun StatusBarPreview() {
    KeiTheme {
        StatusBar(
            page = EditorPage.Profile,
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(KeiTheme.colors.desk)
                .padding(horizontal = ProfileDimensions.DeskPadding + 4.dp, vertical = 6.dp),
        )
    }
}

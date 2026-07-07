@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.core.designsystem.theme.KeiTheme
import io.github.kei_1111.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.feature.profile.theme.themedIcon
import kei_1111.feature.profile.generated.resources.Res
import kei_1111.feature.profile.generated.resources.ic_inspections_ok_dark
import org.jetbrains.compose.resources.painterResource

/**
 * ステータスバー。デスク上に直接。左にパンくず、右に位置/改行/エンコード情報。
 * デスクからの余白は親が設定する。
 */
@Composable
internal fun StatusBar(
    page: EditorPage,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = page.breadcrumb,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            style = KeiTheme.typography.chrome.copy(fontSize = 12.sp, color = KeiTheme.colors.textSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusItem("1:1")
            StatusItem("LF")
            StatusItem("UTF-8")
            StatusItem("4 spaces")
            Icon(
                painter = painterResource(themedIcon(Res.drawable.ic_inspections_ok_dark)),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
private fun StatusItem(
    text: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = KeiTheme.colors.mutedHigh,
) {
    Text(
        text = text,
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(fontSize = 12.sp, color = color),
    )
}

@Preview
@Composable
private fun StatusBarPreview() {
    KeiTheme {
        StatusBar(
            page = EditorPage.Profile,
            modifier = Modifier
                .fillMaxWidth()
                .background(KeiTheme.colors.desk)
                .padding(horizontal = ProfileDimensions.DeskPadding + 4.dp, vertical = 6.dp),
        )
    }
}

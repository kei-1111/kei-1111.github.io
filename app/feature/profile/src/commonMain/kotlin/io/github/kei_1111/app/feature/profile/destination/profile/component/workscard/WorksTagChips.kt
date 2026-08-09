@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.component.workscard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.shared.model.WorkTag

/**
 * WorksPreviewCard / WorksSheet の両方が使うタグチップ。
 * accent タグ（言語・UI系）は緑、それ以外は textSecondary で塗り分ける。
 */
@Composable
internal fun WorksTagChip(
    tag: WorkTag,
    modifier: Modifier = Modifier,
) {
    val textColor = if (tag.accent) KeiTheme.colors.androidGreen else KeiTheme.colors.textSecondary
    WorksChipSurface(text = tag.name, textColor = textColor, modifier = modifier)
}

/** カードのチップ行だけが使う「+n」オーバーフローチップ。クリック不可（全量はシートで見せる）。 */
@Composable
internal fun WorksTagOverflowChip(
    count: Int,
    modifier: Modifier = Modifier,
) {
    WorksChipSurface(text = "+$count", textColor = KeiTheme.colors.textSecondary, modifier = modifier)
}

@Composable
private fun WorksChipSurface(
    text: String,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(KeiTheme.shapes.pill)
            .background(KeiTheme.colors.gitHubItem)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = KeiTheme.typography.chrome.copy(fontSize = 7.sp, color = textColor),
        )
    }
}

@Preview
@Composable
private fun WorksTagChipPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.cardBackground).padding(8.dp)) {
            Row {
                WorksTagChip(tag = WorkTag(name = "Kotlin", accent = true))
                Spacer(modifier = Modifier.size(6.dp))
                WorksTagChip(tag = WorkTag(name = "detekt"))
                Spacer(modifier = Modifier.size(6.dp))
                WorksTagOverflowChip(count = 2)
            }
        }
    }
}

@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.component.workscard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.brandColor
import io.github.kei_1111.app.core.designsystem.theme.icon
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.WorkTag
import org.jetbrains.compose.resources.painterResource

/**
 * WorksPreviewCard / WorksDetailSheet の両方が使う、タグチップとリンクボタン。
 * accent タグ（言語・UI系）は緑、それ以外は textSecondary で塗り分ける。
 */
@Composable
internal fun WorksTagChip(
    tag: WorkTag,
    modifier: Modifier = Modifier,
) {
    val textColor = if (tag.accent) KeiTheme.colors.androidGreen else KeiTheme.colors.textSecondary
    Box(
        modifier = modifier
            .clip(KeiTheme.shapes.pill)
            .background(KeiTheme.colors.gitHubItem)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = tag.name,
            style = KeiTheme.typography.chrome.copy(fontSize = 7.sp, color = textColor),
        )
    }
}

/** カードのチップ行だけが使う「+n」オーバーフローチップ。クリック不可（全量はシートで見せる）。 */
@Composable
internal fun WorksTagOverflowChip(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(KeiTheme.shapes.pill)
            .background(KeiTheme.colors.gitHubItem)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = "+$count",
            style = KeiTheme.typography.chrome.copy(fontSize = 7.sp, color = KeiTheme.colors.textSecondary),
        )
    }
}

/** Google Play リンク。ボタン地は androidGreen、アイコンはラベルと同色に合わせる。 */
@Composable
internal fun WorksStoreButton(
    url: String,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    WorksPrimaryButtonSurface(url = url, onClickUrl = onClickUrl, modifier = modifier) {
        KeiIcon(
            icon = KeiTheme.icons.run,
            contentDescription = null,
            tint = KeiTheme.colors.cardBackground,
            modifier = Modifier.size(13.dp),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(text = "Google Play", style = worksPrimaryButtonLabelStyle())
    }
}

/** ソースリポジトリへのリンク。アイコンは GitHub のブランド色のまま（ラベル色には合わせない）。 */
@Composable
internal fun WorksSourceButton(
    url: String,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    WorksPrimaryButtonSurface(url = url, onClickUrl = onClickUrl, modifier = modifier) {
        Icon(
            painter = painterResource(LinkServiceType.GitHub.icon(KeiTheme.colors)),
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = LinkServiceType.GitHub.brandColor(KeiTheme.colors),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(text = "Source", style = worksPrimaryButtonLabelStyle())
    }
}

@Composable
private fun WorksPrimaryButtonSurface(
    url: String,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(KeiTheme.shapes.githubItem)
            .background(KeiTheme.colors.androidGreen)
            .clickable { onClickUrl(url) },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, content = content)
    }
}

/** アイコンと同じ Row に leaf として並べるため、専用コンポーネントには分けずスタイルだけ共有する。 */
@Composable
private fun worksPrimaryButtonLabelStyle() = KeiTheme.typography.chrome.copy(
    fontSize = 10.sp,
    fontWeight = FontWeight.Bold,
    color = KeiTheme.colors.cardBackground,
)

@Preview
@Composable
private fun WorksSharedComponentsPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.cardBackground).padding(8.dp)) {
            Row {
                WorksTagChip(tag = WorkTag(name = "Kotlin", accent = true))
                Spacer(modifier = Modifier.size(6.dp))
                WorksTagChip(tag = WorkTag(name = "detekt"))
                Spacer(modifier = Modifier.size(6.dp))
                WorksTagOverflowChip(count = 2)
                Spacer(modifier = Modifier.size(6.dp))
                WorksStoreButton(url = "https://example.com", onClickUrl = {})
                Spacer(modifier = Modifier.size(6.dp))
                WorksSourceButton(url = "https://example.com", onClickUrl = {})
            }
        }
    }
}

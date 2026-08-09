@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.component.workscard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.brandColor
import io.github.kei_1111.app.core.designsystem.theme.icon
import io.github.kei_1111.app.core.ui.rememberHoverState
import io.github.kei_1111.shared.model.LinkServiceType
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.ic_play_store
import org.jetbrains.compose.resources.painterResource

/**
 * Google Play リンク。GitHubPreviewCard の LinkTile と同じ様式（gitHubItem 面 + ブランドアイコン +
 * 太字ラベル、ホバーでブランド色ボーダー）。公式 Play ロゴは公式カラーのまま描く。
 */
@Composable
internal fun WorksStoreButton(
    url: String,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    WorksLinkButton(
        label = "Google Play",
        hoverBorderColor = KeiTheme.colors.androidGreen,
        url = url,
        onClickUrl = onClickUrl,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_play_store),
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = Color.Unspecified,
        )
    }
}

/** ソースリポジトリへのリンク。アイコン・ホバー色とも LinkTile の GitHub と同じブランド色。 */
@Composable
internal fun WorksSourceButton(
    url: String,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val brandColor = LinkServiceType.GitHub.brandColor(KeiTheme.colors)
    WorksLinkButton(
        label = "Source",
        hoverBorderColor = brandColor,
        url = url,
        onClickUrl = onClickUrl,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(LinkServiceType.GitHub.icon(KeiTheme.colors)),
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = brandColor,
        )
    }
}

@Composable
private fun WorksLinkButton(
    label: String,
    hoverBorderColor: Color,
    url: String,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val hoverState = rememberHoverState()
    val borderColor = if (hoverState.hovered) hoverBorderColor else Color.Transparent
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(KeiTheme.shapes.linkTile)
            .background(KeiTheme.colors.gitHubItem)
            .border(1.dp, borderColor, KeiTheme.shapes.linkTile)
            .hoverable(hoverState.interactionSource)
            .clickable(interactionSource = hoverState.interactionSource, indication = null) { onClickUrl(url) },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(modifier = Modifier.size(7.dp))
            Text(
                text = label,
                style = KeiTheme.typography.githubJp.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Preview
@Composable
private fun WorksStoreButtonPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.cardBackground).padding(8.dp)) {
            Row {
                WorksStoreButton(url = "https://example.com", onClickUrl = {})
                Spacer(modifier = Modifier.size(6.dp))
                WorksSourceButton(url = "https://example.com", onClickUrl = {})
            }
        }
    }
}

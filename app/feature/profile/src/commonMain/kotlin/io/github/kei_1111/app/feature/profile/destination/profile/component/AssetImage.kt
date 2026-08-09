package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Size
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.utils.appOrigin

/** 配布物同梱アセットの相対パスを配信オリジンの絶対 URL へ解決する。http(s) はそのまま。 */
internal fun resolveAssetUrl(url: String): String =
    if (url.startsWith("http")) url else "${appOrigin()}/${url.trimStart('/')}"

/**
 * 配布物同梱アセットの共通ローダー。Coil の既定はレイアウトサイズへ縮小デコードするため、Preview の
 * ズーム拡大や Retina 表示でぼやける。原寸のままデコードし、高品質フィルタで描画する。
 */
@Composable
internal fun AssetAsyncImage(
    url: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = assetImageRequest(url),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.High,
        modifier = modifier,
    )
}

/** [AssetAsyncImage] と同じ原寸デコード設定の painter 版（読み込み後の実比率を参照したい呼び出し側用）。 */
@Composable
internal fun rememberAssetAsyncPainter(url: String): AsyncImagePainter =
    rememberAsyncImagePainter(model = assetImageRequest(url), filterQuality = FilterQuality.High)

@Composable
private fun assetImageRequest(url: String): ImageRequest =
    ImageRequest.Builder(LocalPlatformContext.current)
        .data(resolveAssetUrl(url))
        .size(Size.ORIGINAL)
        .build()

@Preview
@Composable
private fun AssetAsyncImagePreview() {
    KeiTheme {
        AssetAsyncImage(
            url = "images/profile-icon.webp",
            modifier = Modifier.size(56.dp),
        )
    }
}

package io.github.kei_1111.app.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Size
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.utils.appOrigin
import kotlinx.collections.immutable.ImmutableList

/**
 * リモート/配布物同梱アセット画像の共通ローダー。Coil の既定はレイアウトサイズへ縮小デコードするため、
 * Preview のズーム拡大や Retina 表示でぼやける。原寸のままデコードし、高品質フィルタで描画する。
 */
@Composable
fun KeiAsyncImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = assetImageRequest(LocalPlatformContext.current, url),
        contentDescription = contentDescription,
        contentScale = contentScale,
        filterQuality = FilterQuality.High,
        modifier = modifier,
    )
}

/** [KeiAsyncImage] と同じ原寸デコード設定の painter 版（読み込み後の実比率を参照したい呼び出し側用）。 */
@Composable
fun rememberKeiAsyncImagePainter(url: String): AsyncImagePainter =
    rememberAsyncImagePainter(
        model = assetImageRequest(LocalPlatformContext.current, url),
        filterQuality = FilterQuality.High,
    )

/**
 * 表示時のリクエストと [assetImageRequest] を共有するため、Coil のキャッシュキーが一致して読み直しにならない。
 * enqueue は ImageLoader 自身のスコープで走るので、呼び出し元がコンポジションを抜けても中断されない。
 */
@Composable
fun KeiAsyncImagePrefetch(urls: ImmutableList<String>) {
    val context = LocalPlatformContext.current
    LaunchedEffect(urls) {
        val imageLoader = SingletonImageLoader.get(context)
        urls.forEach { imageLoader.enqueue(assetImageRequest(context, it)) }
    }
}

private fun assetImageRequest(context: PlatformContext, url: String): ImageRequest =
    ImageRequest.Builder(context)
        .data(resolveAssetUrl(url))
        .size(Size.ORIGINAL)
        .build()

/** 相対パスを配信オリジンの絶対 URL へ解決する。http(s) はそのまま。 */
private fun resolveAssetUrl(url: String): String =
    if (url.startsWith("http")) url else "${appOrigin()}/${url.trimStart('/')}"

@Preview
@Composable
private fun KeiAsyncImagePreview() {
    KeiTheme {
        KeiAsyncImage(
            url = "images/profile-icon.webp",
            contentDescription = null,
            modifier = Modifier.size(56.dp),
        )
    }
}

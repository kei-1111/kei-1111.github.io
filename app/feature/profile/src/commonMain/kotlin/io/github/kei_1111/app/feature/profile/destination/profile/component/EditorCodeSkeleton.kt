@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.destination.profile.theme.rememberSkeletonShimmer
import io.github.kei_1111.app.feature.profile.destination.profile.theme.skeletonShimmer

/**
 * (インデント段数, 幅比率) の固定パターン。ProfileScreen.kt の Kotlin コード構造を模した
 * 疑似コード行を、ランダムなしで決定的に描く。
 */
private val SkeletonRows = listOf(
    0 to 0.30f,
    0 to 0.55f,
    1 to 0.45f,
    1 to 0.65f,
    0 to 0.20f,
    0 to 0.42f,
    1 to 0.50f,
    1 to 0.70f,
    2 to 0.40f,
    2 to 0.55f,
    1 to 0.35f,
    1 to 0.60f,
    0 to 0.25f,
    0 to 0.45f,
    1 to 0.50f,
)

/** [animated] が false のとき（取得失敗で進行していないとき）はシマーを静止させる。 */
@Composable
internal fun EditorCodeSkeleton(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
) {
    val shimmer = rememberSkeletonShimmer(animated = animated)
    val colors = KeiTheme.colors
    val shape = KeiTheme.shapes.chip
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 12.dp),
    ) {
        SkeletonRows.forEach { (indent, widthFraction) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ProfileDimensions.EditorLineHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(ProfileDimensions.SkeletonIndentStep * indent))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(widthFraction)
                        .height(ProfileDimensions.SkeletonBarHeight)
                        .skeletonShimmer(shimmer = shimmer, colors = colors, shape = shape),
                )
            }
        }
    }
}

@Preview
@Composable
private fun EditorCodeSkeletonPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 420.dp, height = 480.dp)
                .background(KeiTheme.colors.island),
        ) {
            EditorCodeSkeleton()
        }
    }
}

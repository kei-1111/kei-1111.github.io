@file:Suppress("MagicNumber", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiColorScheme
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.component.ReadmeBlocks
import io.github.kei_1111.app.feature.profile.destination.profile.theme.appendLink
import kotlinx.collections.immutable.ImmutableList

/** README 用の Markdown プレビューペイン。IntelliJ の Markdown プレビューを模す（ズームツールバー無し）。 */
@Composable
internal fun MarkdownPreviewPane(
    blocks: ImmutableList<MarkdownBlock>,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KeiTheme.colors
    val monoFontFamily = KeiTheme.typography.code.fontFamily
    // AnnotatedString をキャッシュしたまま常に最新の onClickUrl を呼ぶための State
    val currentOnClickUrl = rememberUpdatedState(onClickUrl)
    val bodyStyle = KeiTheme.typography.cardJp.copy(
        fontSize = 13.sp,
        lineHeight = 21.sp,
        color = colors.textPrimary,
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MarkdownBlock.Heading -> Text(
                    text = rememberMarkdownInlines(block.inlines, monoFontFamily, colors, currentOnClickUrl),
                    modifier = if (index == 0) Modifier else Modifier.padding(top = 8.dp),
                    style = bodyStyle.copy(
                        fontSize = when (block.level) {
                            1 -> 22.sp
                            2 -> 17.sp
                            else -> 14.sp
                        },
                        lineHeight = when (block.level) {
                            1 -> 30.sp
                            2 -> 24.sp
                            else -> 20.sp
                        },
                        fontWeight = FontWeight.SemiBold,
                    ),
                )

                is MarkdownBlock.Paragraph -> Text(
                    text = rememberMarkdownInlines(block.inlines, monoFontFamily, colors, currentOnClickUrl),
                    style = bodyStyle,
                )

                is MarkdownBlock.BulletList -> MarkdownBulletList(
                    items = block.items,
                    monoFontFamily = monoFontFamily,
                    colors = colors,
                    onClickUrl = currentOnClickUrl,
                    bodyStyle = bodyStyle,
                )
            }
        }
    }
}

@Composable
private fun MarkdownBulletList(
    items: List<List<MarkdownInline>>,
    monoFontFamily: FontFamily?,
    colors: KeiColorScheme,
    onClickUrl: State<(String) -> Unit>,
    bodyStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                BulletMark(color = bodyStyle.color)
                Text(
                    text = rememberMarkdownInlines(item, monoFontFamily, colors, onClickUrl),
                    modifier = Modifier.weight(1f),
                    style = bodyStyle,
                )
            }
        }
    }
}

/** リスト項目の丸ビュレット。バンドルフォントに • (U+2022) のグリフが無く、wasm ではフォールバック先も無いため円を描画する。 */
@Composable
private fun BulletMark(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        // 本文1行目（lineHeight 21.sp）の中央に合わせる
        modifier = modifier
            .width(16.dp)
            .height(21.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(color = color, shape = CircleShape),
        )
    }
}

/** インライン列のレンダリング結果を、再コンポーズをまたいで保持する。 */
@Composable
private fun rememberMarkdownInlines(
    inlines: List<MarkdownInline>,
    monoFontFamily: FontFamily?,
    colors: KeiColorScheme,
    onClickUrl: State<(String) -> Unit>,
): AnnotatedString = remember(inlines, monoFontFamily, colors) {
    markdownInlinesOf(inlines, monoFontFamily, colors) { onClickUrl.value(it) }
}

private fun markdownInlinesOf(
    inlines: List<MarkdownInline>,
    monoFontFamily: FontFamily?,
    colors: KeiColorScheme,
    onClickUrl: (String) -> Unit,
): AnnotatedString = buildAnnotatedString {
    inlines.forEach { inline ->
        when (inline) {
            is MarkdownInline.PlainText -> append(inline.text)
            is MarkdownInline.InlineCode -> withStyle(
                SpanStyle(
                    fontFamily = monoFontFamily,
                    fontSize = 12.sp,
                    background = colors.chip,
                    color = colors.textCode,
                ),
            ) {
                append(inline.text)
            }

            is MarkdownInline.Link -> appendLink(
                text = inline.text,
                url = inline.url,
                colors = colors,
                // 既定の UriHandler ではなく、Effect 経由の openUrl() で開く
                linkInteractionListener = { onClickUrl(inline.url) },
            )
        }
    }
}

@Preview
@Composable
private fun MarkdownPreviewPanePreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 420.dp, height = 640.dp)
                .background(KeiTheme.colors.island),
        ) {
            MarkdownPreviewPane(blocks = ReadmeBlocks, onClickUrl = {})
        }
    }
}

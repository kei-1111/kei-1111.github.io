package io.github.kei_1111.app.feature.profile.destination.profile.component.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import io.github.kei_1111.app.core.designsystem.theme.KeiColorScheme
import io.github.kei_1111.app.feature.profile.theme.appendLink
import io.github.kei_1111.app.feature.profile.theme.withJapaneseFont

/**
 * ブロック列から、エディタペイン表示用にハイライトした Markdown ソースの行リストを構築する。
 * ソーステキストへの直列化と再パースを経由せず [MarkdownBlock] を直接走査するため、
 * コンテンツがどんな文字を含んでもプレビュー（MarkdownPreviewPane）と構造が食い違わない。
 */
internal fun highlightMarkdown(
    blocks: List<MarkdownBlock>,
    japaneseFontFamily: FontFamily,
    colors: KeiColorScheme,
): List<AnnotatedString> = buildList {
    blocks.forEachIndexed { index, block ->
        // Markdown ソースのブロック間空行に相当する行
        if (index > 0) add(AnnotatedString(""))
        when (block) {
            is MarkdownBlock.Heading -> add(headingLine(block, colors))
            is MarkdownBlock.Paragraph -> add(paragraphLine(block, colors))
            is MarkdownBlock.BulletList -> block.items.forEach { add(bulletLine(it, colors)) }
        }
    }
}.map { it.withJapaneseFont(japaneseFontFamily) }

private fun headingLine(block: MarkdownBlock.Heading, colors: KeiColorScheme): AnnotatedString =
    buildAnnotatedString {
        val baseStyle = SpanStyle(color = colors.textCode, fontWeight = FontWeight.Bold)
        withStyle(SpanStyle(color = colors.syntaxKeyword)) { append("#".repeat(block.level)) }
        withStyle(baseStyle) { append(" ") }
        appendInlineSource(block.inlines, colors, baseStyle)
    }

private fun paragraphLine(block: MarkdownBlock.Paragraph, colors: KeiColorScheme): AnnotatedString =
    buildAnnotatedString {
        appendInlineSource(block.inlines, colors, SpanStyle(color = colors.textCode))
    }

private fun bulletLine(item: List<MarkdownInline>, colors: KeiColorScheme): AnnotatedString =
    buildAnnotatedString {
        val baseStyle = SpanStyle(color = colors.textCode)
        withStyle(SpanStyle(color = colors.syntaxKeyword)) { append("-") }
        withStyle(baseStyle) { append(" ") }
        appendInlineSource(item, colors, baseStyle)
    }

/** インライン列を Markdown ソース表記（`` `code` `` / `[text](url)`）のままハイライトして追加する。 */
private fun AnnotatedString.Builder.appendInlineSource(
    inlines: List<MarkdownInline>,
    colors: KeiColorScheme,
    baseStyle: SpanStyle,
) {
    inlines.forEach { inline ->
        when (inline) {
            is MarkdownInline.PlainText -> withStyle(baseStyle) { append(inline.text) }

            is MarkdownInline.InlineCode -> withStyle(baseStyle) {
                withStyle(SpanStyle(color = colors.syntaxString)) { append("`${inline.text}`") }
            }

            is MarkdownInline.Link -> {
                withStyle(baseStyle) { append("[${inline.text}](") }
                appendLink(text = inline.url, url = inline.url, colors = colors)
                withStyle(baseStyle) { append(")") }
            }
        }
    }
}

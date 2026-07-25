package io.github.kei_1111.app.feature.profile.destination.profile.component.markdown

import androidx.compose.runtime.Immutable

/** README を構成する Markdown ブロック。エディタのソース表示とプレビュー描画が共有する唯一のデータ。 */
@Immutable
internal sealed interface MarkdownBlock {
    data class Heading(val level: Int, val inlines: List<MarkdownInline>) : MarkdownBlock
    data class Paragraph(val inlines: List<MarkdownInline>) : MarkdownBlock
    data class BulletList(val items: List<List<MarkdownInline>>) : MarkdownBlock
}

/** ブロック内のインライン要素。 */
@Immutable
internal sealed interface MarkdownInline {
    data class PlainText(val text: String) : MarkdownInline
    data class InlineCode(val text: String) : MarkdownInline
    data class Link(val text: String, val url: String) : MarkdownInline
}

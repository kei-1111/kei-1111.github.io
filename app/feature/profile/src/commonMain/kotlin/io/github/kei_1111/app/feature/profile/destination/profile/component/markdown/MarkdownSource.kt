package io.github.kei_1111.app.feature.profile.destination.profile.component.markdown

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private val headingRegex = Regex("""^(#{1,6}) (.*)$""")
private val bulletRegex = Regex("""^- (.*)$""")
private val inlineRegex = Regex("""`([^`]+)`|\[([^\]]*)\]\(([^)\s]*)\)""")

/** ブロック列を Markdown ソーステキストへ直列化する（編集フィールドの初期表示用）。 */
internal fun markdownSource(blocks: List<MarkdownBlock>): String = blocks.joinToString("\n\n") { block ->
    when (block) {
        is MarkdownBlock.Heading -> "${"#".repeat(block.level)} ${inlineSource(block.inlines)}"
        is MarkdownBlock.Paragraph -> inlineSource(block.inlines)
        is MarkdownBlock.BulletList -> block.items.joinToString("\n") { "- ${inlineSource(it)}" }
    }
}

/** Markdown テキストをブロック列へパースする。Markdown に不正はないため常に成功する。 */
internal fun parseMarkdown(text: String): ImmutableList<MarkdownBlock> {
    val lines = text.split('\n')
    val blocks = mutableListOf<MarkdownBlock>()
    var index = 0
    while (index < lines.size) {
        val line = lines[index]
        when {
            line.isBlank() -> index++
            headingRegex.matches(line) -> {
                blocks += parseHeading(line)
                index++
            }

            bulletRegex.matches(line) -> {
                val (bulletList, nextIndex) = parseBulletList(lines, index)
                blocks += bulletList
                index = nextIndex
            }

            else -> {
                blocks += MarkdownBlock.Paragraph(parseInlines(line))
                index++
            }
        }
    }
    return blocks.toImmutableList()
}

private fun inlineSource(inlines: List<MarkdownInline>): String = inlines.joinToString("") { inline ->
    when (inline) {
        is MarkdownInline.PlainText -> inline.text
        is MarkdownInline.InlineCode -> "`${inline.text}`"
        is MarkdownInline.Link -> "[${inline.text}](${inline.url})"
    }
}

private fun parseHeading(line: String): MarkdownBlock.Heading {
    val match = requireNotNull(headingRegex.matchEntire(line))
    return MarkdownBlock.Heading(
        level = match.groupValues[1].length,
        inlines = parseInlines(match.groupValues[2]),
    )
}

private fun parseBulletList(lines: List<String>, startIndex: Int): Pair<MarkdownBlock.BulletList, Int> {
    val items = mutableListOf<List<MarkdownInline>>()
    var index = startIndex
    while (index < lines.size) {
        val match = bulletRegex.matchEntire(lines[index]) ?: break
        items += parseInlines(match.groupValues[1])
        index++
    }
    return MarkdownBlock.BulletList(items) to index
}

private fun parseInlines(text: String): List<MarkdownInline> {
    val inlines = mutableListOf<MarkdownInline>()
    var cursor = 0
    inlineRegex.findAll(text).forEach { match ->
        if (cursor < match.range.first) {
            inlines += MarkdownInline.PlainText(text.substring(cursor, match.range.first))
        }
        inlines += if (match.groups[1] != null) {
            MarkdownInline.InlineCode(match.groupValues[1])
        } else {
            MarkdownInline.Link(text = match.groupValues[2], url = match.groupValues[3])
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) inlines += MarkdownInline.PlainText(text.substring(cursor))
    return inlines
}

package io.github.kei_1111.app.feature.profile.destination.profile.component.markdown

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.kei_1111.app.core.designsystem.theme.KeiDarkColorScheme
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.MarkdownListItem
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkdownHighlighterTest {

    private fun lines(vararg blocks: MarkdownBlock): List<String> =
        highlightMarkdown(blocks.toList(), FontFamily.Default, KeiDarkColorScheme).map { it.text }

    @Test
    fun rendersEachBlockKindInMarkdownSourceNotation() {
        assertEquals(
            listOf("## Title", "", "body `code`", "", "- item"),
            lines(
                MarkdownBlock.Heading(level = 2, inlines = persistentListOf(MarkdownInline.PlainText("Title"))),
                MarkdownBlock.Paragraph(
                    inlines = persistentListOf(
                        MarkdownInline.PlainText("body "),
                        MarkdownInline.InlineCode("code"),
                    ),
                ),
                MarkdownBlock.BulletList(
                    items = persistentListOf(MarkdownListItem(persistentListOf(MarkdownInline.PlainText("item")))),
                ),
            ),
        )
    }

    @Test
    fun rendersLinksInSourceNotation() {
        assertEquals(
            listOf("[Compose](https://example.com)"),
            lines(
                MarkdownBlock.Paragraph(
                    inlines = persistentListOf(MarkdownInline.Link(text = "Compose", url = "https://example.com")),
                ),
            ),
        )
    }

    @Test
    fun stylesHeadingHashesAsKeywordAndBodyAsBold() {
        val heading = highlightMarkdown(
            listOf(MarkdownBlock.Heading(level = 2, inlines = persistentListOf(MarkdownInline.PlainText("Title")))),
            FontFamily.Default,
            KeiDarkColorScheme,
        ).single()

        val hashStyle = heading.spanStyles.first { it.start == 0 }
        assertEquals(KeiDarkColorScheme.syntaxKeyword, hashStyle.item.color)
        assertEquals(2, hashStyle.end)
        assertTrue(heading.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    }

    @Test
    fun stylesInlineCodeAsStringColor() {
        val paragraph = highlightMarkdown(
            listOf(MarkdownBlock.Paragraph(inlines = persistentListOf(MarkdownInline.InlineCode("x")))),
            FontFamily.Default,
            KeiDarkColorScheme,
        ).single()

        assertTrue(paragraph.spanStyles.any { it.item.color == KeiDarkColorScheme.syntaxString })
    }
}

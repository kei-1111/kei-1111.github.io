package io.github.kei_1111.app.feature.profile.destination.profile.component.markdown

import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.MarkdownListItem
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownSourceTest {

    private val blocks = persistentListOf(
        MarkdownBlock.Heading(
            level = 2,
            inlines = persistentListOf(MarkdownInline.PlainText("Tech stack")),
        ),
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(
                MarkdownInline.PlainText("Built with "),
                MarkdownInline.InlineCode("Kotlin"),
                MarkdownInline.PlainText(" and "),
                MarkdownInline.Link(text = "Compose", url = "https://example.com/compose"),
                MarkdownInline.PlainText("."),
            ),
        ),
        MarkdownBlock.BulletList(
            items = persistentListOf(
                MarkdownListItem(persistentListOf(MarkdownInline.PlainText("first"))),
                MarkdownListItem(persistentListOf(MarkdownInline.InlineCode("second"))),
            ),
        ),
    )

    @Test
    fun roundTripsGeneratedMarkdownSource() {
        assertEquals(blocks, parseMarkdown(markdownSource(blocks)))
    }

    @Test
    fun serializesEachBlockKindToItsMarkdownNotation() {
        assertEquals(
            "## Tech stack\n\nBuilt with `Kotlin` and [Compose](https://example.com/compose).\n\n- first\n- `second`",
            markdownSource(blocks),
        )
    }

    @Test
    fun parsesConsecutiveBulletsIntoOneList() {
        val parsed = parseMarkdown("- a\n- b")

        assertEquals(
            persistentListOf<MarkdownBlock>(
                MarkdownBlock.BulletList(
                    items = persistentListOf(
                        MarkdownListItem(persistentListOf(MarkdownInline.PlainText("a"))),
                        MarkdownListItem(persistentListOf(MarkdownInline.PlainText("b"))),
                    ),
                ),
            ),
            parsed,
        )
    }

    @Test
    fun splitsBulletListsSeparatedByAParagraph() {
        val parsed = parseMarkdown("- a\ntext\n- b")

        assertEquals(3, parsed.size)
        assertEquals(
            listOf(MarkdownBlock.BulletList::class, MarkdownBlock.Paragraph::class, MarkdownBlock.BulletList::class),
            parsed.map { it::class },
        )
    }

    @Test
    fun ignoresBlankLines() {
        assertEquals(
            persistentListOf<MarkdownBlock>(
                MarkdownBlock.Paragraph(persistentListOf(MarkdownInline.PlainText("a"))),
                MarkdownBlock.Paragraph(persistentListOf(MarkdownInline.PlainText("b"))),
            ),
            parseMarkdown("a\n\n\nb"),
        )
    }

    @Test
    fun keepsASevenHashLineAsAParagraph() {
        // heading は 6 レベルまで。7 個はそのまま本文になる
        assertEquals(
            persistentListOf<MarkdownBlock>(
                MarkdownBlock.Paragraph(persistentListOf(MarkdownInline.PlainText("####### x"))),
            ),
            parseMarkdown("####### x"),
        )
    }
}

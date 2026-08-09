package io.github.kei_1111.shared.model.serialization

import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.MarkdownListItem
import io.github.kei_1111.shared.model.Readme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

private val json = Json

class ReadmeSerializationTest {

    @Test
    fun roundTripsAReadmeWithEveryBlockAndInlineKind() {
        val readme = readmeWithEveryKind()

        val decoded = json.decodeFromString<Readme>(json.encodeToString(Readme.serializer(), readme))

        assertEquals(readme, decoded)
    }

    @Test
    fun encodesSubtypeDiscriminatorsAsStableTypeKeys() {
        val readme = readmeWithEveryKind()

        val encoded = json.encodeToString(Readme.serializer(), readme)
        val root = json.parseToJsonElement(encoded).jsonObject
        val blocks = root.getValue("ja").jsonArray
        val paragraphInlines = blocks[1].jsonObject.getValue("inlines").jsonArray

        assertEquals(
            listOf("heading", "paragraph", "bullet_list"),
            blocks.map { it.jsonObject.getValue("type").jsonPrimitive.content },
        )
        assertEquals(
            listOf("plain_text", "inline_code", "link"),
            paragraphInlines.map { it.jsonObject.getValue("type").jsonPrimitive.content },
        )
    }
}

private fun readmeWithEveryKind() = Readme(
    ja = persistentListOf(
        MarkdownBlock.Heading(
            level = 1,
            inlines = persistentListOf(MarkdownInline.PlainText("README")),
        ),
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(
                MarkdownInline.PlainText("Run "),
                MarkdownInline.InlineCode("./gradlew test"),
                MarkdownInline.Link(text = "Repository", url = "https://example.com"),
            ),
        ),
        MarkdownBlock.BulletList(
            items = persistentListOf(
                MarkdownListItem(persistentListOf(MarkdownInline.PlainText("First"))),
                MarkdownListItem(persistentListOf(MarkdownInline.PlainText("Second"))),
            ),
        ),
    ),
    en = persistentListOf(),
)

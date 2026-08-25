package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface MarkdownBlock {
    @Serializable
    @SerialName("heading")
    data class Heading(
        @SerialName("level")
        val level: Int,
        @Serializable(with = ImmutableListSerializer::class)
        @SerialName("inlines")
        val inlines: ImmutableList<MarkdownInline> = persistentListOf(),
    ) : MarkdownBlock

    @Serializable
    @SerialName("paragraph")
    data class Paragraph(
        @Serializable(with = ImmutableListSerializer::class)
        @SerialName("inlines")
        val inlines: ImmutableList<MarkdownInline> = persistentListOf(),
    ) : MarkdownBlock

    @Serializable
    @SerialName("bullet_list")
    data class BulletList(
        @Serializable(with = ImmutableListSerializer::class)
        @SerialName("items")
        val items: ImmutableList<MarkdownListItem> = persistentListOf(),
    ) : MarkdownBlock
}

@Serializable
data class MarkdownListItem(
    @Serializable(with = ImmutableListSerializer::class)
    @SerialName("inlines")
    val inlines: ImmutableList<MarkdownInline> = persistentListOf(),
)

@Serializable
sealed interface MarkdownInline {
    @Serializable
    @SerialName("plain_text")
    data class PlainText(
        @SerialName("text")
        val text: String,
    ) : MarkdownInline

    @Serializable
    @SerialName("inline_code")
    data class InlineCode(
        @SerialName("text")
        val text: String,
    ) : MarkdownInline

    @Serializable
    @SerialName("link")
    data class Link(
        @SerialName("text")
        val text: String,
        @SerialName("url")
        val url: String,
    ) : MarkdownInline
}

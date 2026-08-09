package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** client / server 間で共有する README 本文。表示言語の解決はクライアント側で行う。 */
@Serializable
data class Readme(
    @Serializable(with = ImmutableListSerializer::class)
    @SerialName("ja")
    val ja: ImmutableList<MarkdownBlock> = persistentListOf(),
    @Serializable(with = ImmutableListSerializer::class)
    @SerialName("en")
    val en: ImmutableList<MarkdownBlock> = persistentListOf(),
)

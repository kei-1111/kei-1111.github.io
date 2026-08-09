package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TerminalTextCommand(
    @SerialName("keyword")
    val keyword: String,
    @SerialName("description")
    val description: String = "",
    @SerialName("lines")
    @Serializable(with = ImmutableListSerializer::class)
    val lines: ImmutableList<String> = persistentListOf(),
)

/** `GET /api/terminal-commands` のレスポンス全体。他エンドポイント同様、将来のフィールド追加に備えて配列をオブジェクトで包む。 */
@Serializable
data class TerminalTextCommands(
    @SerialName("items")
    @Serializable(with = ImmutableListSerializer::class)
    val items: ImmutableList<TerminalTextCommand> = persistentListOf(),
)

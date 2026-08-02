package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * client / server 間で共有する JSON 契約。互換性ルールは [GitHubProfile] の KDoc を参照。
 */
@Serializable
data class Work(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("stack")
    val stack: String,
    @SerialName("description")
    val description: LocalizedText,
    @SerialName("tags")
    @Serializable(with = ImmutableListSerializer::class)
    val tags: ImmutableList<String>,
    /** 40dp タイル用アイコン。null はクライアント側の既定アイコンを使う。 */
    @SerialName("iconUrl")
    val iconUrl: String? = null,
    @SerialName("screenshots")
    @Serializable(with = ImmutableListSerializer::class)
    val screenshots: ImmutableList<String>,
    @SerialName("storeUrl")
    val storeUrl: String? = null,
    @SerialName("sourceUrl")
    val sourceUrl: String? = null,
)

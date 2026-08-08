package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubIssue(
    @SerialName("number")
    val number: Int,
    @SerialName("title")
    val title: String,
    @SerialName("url")
    val url: String,
    /** Issue タイトルの `[Type]:` プレフィックスから server 側で抽出した種別。プレフィックスが無ければ null。 */
    @SerialName("type")
    val type: String? = null,
)

@Serializable
data class GitHubIssues(
    @SerialName("totalCount")
    val totalCount: Int,
    @SerialName("issues")
    @Serializable(with = ImmutableListSerializer::class)
    val issues: ImmutableList<GitHubIssue>,
)

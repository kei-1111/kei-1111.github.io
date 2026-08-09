package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubPullRequest(
    @SerialName("number")
    val number: Int,
    @SerialName("title")
    val title: String,
    @SerialName("url")
    val url: String,
    @SerialName("headRefName")
    val headRefName: String,
    @SerialName("mergedAt")
    val mergedAt: String,
    /** Pull Request タイトルの `[Type]:` プレフィックスから server 側で抽出した種別。プレフィックスが無ければ null。 */
    @SerialName("type")
    val type: String? = null,
)

@Serializable
data class GitHubChangelog(
    @SerialName("totalCount")
    val totalCount: Int,
    @SerialName("pullRequests")
    @Serializable(with = ImmutableListSerializer::class)
    val pullRequests: ImmutableList<GitHubPullRequest>,
)

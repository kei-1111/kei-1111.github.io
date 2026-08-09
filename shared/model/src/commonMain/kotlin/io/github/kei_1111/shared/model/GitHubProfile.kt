package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import io.github.kei_1111.shared.model.serialization.TolerantLinkServiceListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class GitHubProfile(
    @SerialName("name")
    val name: LocalizedText,
    @SerialName("handle")
    val handle: String,
    @SerialName("location")
    val location: String,
    @SerialName("role")
    val role: String,
    @SerialName("followers")
    val followers: Int,
    @SerialName("following")
    val following: Int,
    @SerialName("repos")
    val repos: Int,
    @SerialName("totalStars")
    val totalStars: Int,
    @SerialName("pinnedRepos")
    @Serializable(with = ImmutableListSerializer::class)
    val pinnedRepos: ImmutableList<PinnedRepo>,
    @SerialName("languages")
    @Serializable(with = ImmutableListSerializer::class)
    val languages: ImmutableList<LanguageShare>,
    @SerialName("links")
    @Serializable(with = TolerantLinkServiceListSerializer::class)
    val links: ImmutableList<LinkService>,
)

@Serializable
data class PinnedRepo(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: LocalizedText,
    @SerialName("url")
    val url: String,
    @SerialName("language")
    val language: RepoLanguage? = null,
    @SerialName("stars")
    val stars: Int? = null,
)

@Serializable
@JvmInline
value class RepoLanguage(val name: String)

@Serializable
data class LanguageShare(
    @SerialName("language")
    val language: RepoLanguage,
    @SerialName("share")
    val share: Float,
    @SerialName("color")
    val color: String? = null,
)

@Serializable
data class LinkService(
    @SerialName("type")
    val type: LinkServiceType,
    @SerialName("name")
    val name: String,
    @SerialName("url")
    val url: String,
)

@Serializable
enum class LinkServiceType {
    @SerialName("GitHub")
    GitHub,

    @SerialName("X")
    X,

    @SerialName("Qiita")
    Qiita,

    @SerialName("Note")
    Note,
}

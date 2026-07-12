package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

/**
 * client / server 間で共有する JSON 契約。
 */
@Serializable
data class GitHubProfile(
    val name: String,
    val handle: String,
    val location: String,
    val role: String,
    val followers: Int,
    val following: Int,
    val repos: Int,
    val totalStars: Int,
    @Serializable(with = ImmutableListSerializer::class)
    val pinnedRepos: ImmutableList<PinnedRepo>,
    @Serializable(with = ImmutableListSerializer::class)
    val languages: ImmutableList<LanguageShare>,
    @Serializable(with = ImmutableListSerializer::class)
    val links: ImmutableList<LinkService>,
)

@Serializable
data class PinnedRepo(
    val name: String,
    val description: String,
    val url: String,
    val language: RepoLanguage? = null,
    val stars: Int? = null,
)

enum class RepoLanguage(val displayName: String) {
    Kotlin("Kotlin"),
    Swift("Swift"),
    Shell("Shell"),
}

@Serializable
data class LanguageShare(
    val language: RepoLanguage,
    val share: Float,
)

@Serializable
data class LinkService(
    val type: LinkServiceType,
    val name: String,
    val url: String,
)

enum class LinkServiceType {
    GitHub,
    X,
    Qiita,
    Note,
}

package io.github.kei_1111.core.model

import kotlinx.collections.immutable.ImmutableList

data class GitHubProfile(
    val name: String,
    val handle: String,
    val location: String,
    val role: String,
    val followers: Int,
    val following: Int,
    val repos: Int,
    val totalStars: Int,
    val pinnedRepos: ImmutableList<PinnedRepo>,
    val languages: ImmutableList<LanguageShare>,
    val links: ImmutableList<LinkService>,
)

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

data class LanguageShare(
    val language: RepoLanguage,
    val share: Float,
)

data class LinkService(
    val type: LinkServiceType,
    val name: String,
    val url: String,
)

enum class LinkServiceType {
    GitHub,
    X,
    Qiita,
    Zenn,
}

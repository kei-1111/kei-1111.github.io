package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * client / server 間で共有する JSON 契約。
 *
 * client(GitHub Pages)と server(Cloud Run)は独立してデプロイされるため、
 * 互換性ルールを守ること:
 * - フィールド追加は必ずデフォルト値付きで行う(新 client + 旧 server で欠損を補える。
 *   旧 client + 新 server は parse 側の ignoreUnknownKeys が吸収する)
 * - フィールド・enum 定数の rename / 削除 / 型変更は wire 破壊 — 移行計画なしに行わない
 * - シリアライズ名は @SerialName で固定し、Kotlin 名の変更が wire に漏れないようにする
 * - 直列化形状は :server の SharedModelContractTest が固定している
 */
@Serializable
data class GitHubProfile(
    @SerialName("name")
    val name: String,
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
    @Serializable(with = ImmutableListSerializer::class)
    val links: ImmutableList<LinkService>,
)

@Serializable
data class PinnedRepo(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("url")
    val url: String,
    @SerialName("language")
    val language: RepoLanguage? = null,
    @SerialName("stars")
    val stars: Int? = null,
)

@Serializable
enum class RepoLanguage(val displayName: String) {
    @SerialName("Kotlin")
    Kotlin("Kotlin"),

    @SerialName("Swift")
    Swift("Swift"),

    @SerialName("Shell")
    Shell("Shell"),
}

@Serializable
data class LanguageShare(
    @SerialName("language")
    val language: RepoLanguage,
    @SerialName("share")
    val share: Float,
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

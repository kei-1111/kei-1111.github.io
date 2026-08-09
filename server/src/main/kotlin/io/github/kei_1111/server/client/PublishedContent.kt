package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.WorkTag
import io.github.kei_1111.shared.model.Works
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

/**
 * 管理コンソール(kei-1111-admin)が GCS の `content/published/` に公開するコンテンツのスキーマ。
 * 契約モデルではなく admin 側の編集モデルそのままのため、ここで契約モデルへ変換する。
 */
@Serializable
data class PublishedWorks(
    val works: List<PublishedWork> = emptyList(),
)

@Serializable
data class PublishedWork(
    val id: String,
    val name: String,
    val type: String = "",
    val period: String = "",
    val about: String = "",
    val aboutEn: String = "",
    val techStack: List<String> = emptyList(),
    val roles: List<String> = emptyList(),
    val rolesEn: List<String> = emptyList(),
    val screenshots: List<String> = emptyList(),
    val googlePlayUrl: String = "",
    val sourceUrl: String = "",
)

/** 公開コンテンツを `GET /api/works` の契約モデルへ変換する。 */
fun PublishedWorks.toWorks(assetBaseUrl: String): Works = Works(
    items = works.map { it.toWork(assetBaseUrl) }.toImmutableList(),
)

private fun PublishedWork.toWork(assetBaseUrl: String): Work = Work(
    id = id,
    name = name,
    kind = type,
    period = period,
    description = localized(ja = about, en = aboutEn),
    tags = techStack.map { WorkTag(name = it, accent = isLanguageOrUiTag(it)) }.toImmutableList(),
    roles = roles.mapIndexed { index, ja ->
        localized(ja = ja, en = rolesEn.getOrNull(index).orEmpty())
    }.toImmutableList(),
    iconUrl = null,
    screenshots = screenshots.map { resolveAssetUrl(it, assetBaseUrl) }.toImmutableList(),
    storeUrl = googlePlayUrl.ifBlank { null },
    sourceUrl = sourceUrl.ifBlank { null },
)

// 本体カードの色分け(admin の techChipKindOf)と同じキーワード規則
private val languageOrUiKeywords = listOf(
    "kotlin",
    "java",
    "swift",
    "dart",
    "typescript",
    "javascript",
    "compose",
    "jetpack compose",
    "compose multiplatform",
    "swiftui",
    "flutter",
    "react",
    "wasm",
)

private fun isLanguageOrUiTag(tag: String): Boolean {
    val normalized = tag.trim().lowercase()
    return languageOrUiKeywords.any { normalized == it || normalized.contains(it) }
}

// admin アップロード規約(images/works/<workId>/<file>)のパスだけ管理サーバー基準の絶対 URL にする。
// それ以外の相対パスはポートフォリオ同梱資産として据え置く(契約: Work KDoc)
private val adminUploadedAssetPattern = Regex("^images/works/[^/]+/.+")

private fun resolveAssetUrl(path: String, assetBaseUrl: String): String = when {
    path.startsWith("http") -> path
    adminUploadedAssetPattern.matches(path) -> "${assetBaseUrl.trimEnd('/')}/$path"
    else -> path
}

/** en が空白のフィールドは ja をそのまま配信する(admin 側の入力規約)。 */
private fun localized(ja: String, en: String): LocalizedText =
    LocalizedText(ja = ja, en = en.ifBlank { ja })

@Serializable
data class PublishedProfile(
    val displayName: String = "",
    val displayNameEn: String = "",
    val role: String = "",
    val location: String = "",
    val xUrl: String = "",
    val pinnedRepos: List<PublishedPinnedRepo> = emptyList(),
    val socialLinks: List<PublishedSocialLink> = emptyList(),
)

@Serializable
data class PublishedPinnedRepo(
    val name: String,
    val visible: Boolean = true,
)

@Serializable
data class PublishedSocialLink(
    val service: String,
    val url: String,
)

/**
 * 管理コンソールで編集可能なフィールドを GitHub 由来のプロフィールへ上書きする。
 * 管理画面の Preview(admin 側 overlayOn)と本番表示を一致させるため、取得できた公開値は
 * 空でも authoritative に扱う — ベースへ戻るのは profile.json 自体が読めない場合だけ。
 * 統計(followers 等)と languages はベースを保つ。
 */
fun PublishedProfile.overlayOn(base: GitHubProfile): GitHubProfile = base.copy(
    name = localized(ja = displayName, en = displayNameEn),
    role = role,
    location = location,
    links = overlaidLinks(),
    pinnedRepos = base.pinnedRepos
        .filter { repo -> pinnedRepos.none { it.name == repo.name && !it.visible } }
        .toImmutableList(),
)

private fun PublishedProfile.overlaidLinks() = buildList {
    socialLinks.forEach { link ->
        linkServiceTypeOf(link.service)?.let { type ->
            add(LinkService(type = type, name = link.service, url = link.url))
        }
    }
    if (xUrl.isNotBlank() && none { it.type == LinkServiceType.X }) {
        add(LinkService(type = LinkServiceType.X, name = "X", url = xUrl))
    }
}.toImmutableList()

private fun linkServiceTypeOf(service: String): LinkServiceType? = when (service.trim().lowercase()) {
    "github" -> LinkServiceType.GitHub
    "x", "twitter" -> LinkServiceType.X
    "qiita" -> LinkServiceType.Qiita
    "note" -> LinkServiceType.Note
    else -> null
}

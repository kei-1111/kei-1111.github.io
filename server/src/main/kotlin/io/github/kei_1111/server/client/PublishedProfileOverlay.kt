package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.Profile
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

@Serializable
internal data class PublishedProfile(
    val displayName: String = "",
    val displayNameEn: String = "",
    val role: String = "",
    val location: String = "",
    val xUrl: String = "",
    val avatarUrl: String = "",
    val pinnedRepos: List<PublishedPinnedRepo> = emptyList(),
    val socialLinks: List<PublishedSocialLink> = emptyList(),
)

@Serializable
internal data class PublishedPinnedRepo(
    val name: String,
    val visible: Boolean = true,
    val descriptionJa: String = "",
    val descriptionEn: String = "",
)

@Serializable
internal data class PublishedSocialLink(
    val service: String,
    val url: String,
)

/**
 * 管理コンソールで編集可能なフィールドを GitHub 由来のプロフィールへ上書きする。
 * 管理画面の Preview(admin 側 overlayOn)と本番表示を一致させるため、取得できた公開値は
 * 空でも authoritative に扱う — ベースへ戻るのは profile.json 自体が読めない場合だけ。
 * 例外は avatarUrl のみ: 空ならベースのアイコンを保つ(未アップロード時に GitHub アバターを残す)。
 * 統計(followers 等)と languages はベースを保つ。
 */
internal fun PublishedProfile.overlayOn(base: Profile): Profile = base.copy(
    name = localized(ja = displayName, en = displayNameEn),
    role = role,
    location = location,
    iconUrl = avatarUrl.ifBlank { base.iconUrl },
    links = overlaidLinks(),
    pinnedRepos = base.pinnedRepos
        .filter { repo -> pinnedRepos.none { it.name == repo.name && !it.visible } }
        .map { repo -> overrideDescription(repo) }
        .toImmutableList(),
)

/** 管理コンソールで説明が上書きされていればそれを使う(空なら GitHub / ビルトインの説明のまま)。 */
private fun PublishedProfile.overrideDescription(repo: PinnedRepo): PinnedRepo {
    val setting = pinnedRepos.firstOrNull { it.name == repo.name }
    return if (setting == null || (setting.descriptionJa.isBlank() && setting.descriptionEn.isBlank())) {
        repo
    } else {
        val ja = setting.descriptionJa.ifBlank { setting.descriptionEn }
        repo.copy(description = LocalizedText(ja = ja, en = setting.descriptionEn.ifBlank { ja }))
    }
}

private fun PublishedProfile.overlaidLinks() = buildList<LinkService> {
    socialLinks.forEach { link ->
        linkServiceTypeOf(link.service)?.let { type ->
            val url = httpUrlOrNull(link.url)
            if (url != null && none { it.type == type }) {
                add(LinkService(type = type, name = displayNameOf(type), url = url))
            }
        }
    }
    if (none { it.type == LinkServiceType.X }) {
        httpUrlOrNull(xUrl)?.let { url ->
            add(LinkService(type = LinkServiceType.X, name = "X", url = url))
        }
    }
}.toImmutableList()

// ビルトインコンテンツ(ProfileContent)と同じ正規表記
private fun displayNameOf(type: LinkServiceType): String = when (type) {
    LinkServiceType.GitHub -> "GitHub"
    LinkServiceType.X -> "X"
    LinkServiceType.Qiita -> "Qiita"
    LinkServiceType.Note -> "note"
}

private fun linkServiceTypeOf(service: String): LinkServiceType? = when (service.trim().lowercase()) {
    "github" -> LinkServiceType.GitHub
    "x", "twitter" -> LinkServiceType.X
    "qiita" -> LinkServiceType.Qiita
    "note" -> LinkServiceType.Note
    else -> null
}

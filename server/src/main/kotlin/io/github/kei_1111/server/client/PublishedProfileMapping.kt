package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
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

/** 管理画面の Preview と本番表示を一致させるため、公開されたリンクが空でもベースへは戻さない。 */
internal fun PublishedProfile.links(): List<LinkService> = buildList {
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
}

/** 管理コンソールで説明が入力されているリポジトリだけを返す(空欄は GitHub の説明のまま)。 */
internal fun PublishedProfile.descriptionOverrides(): Map<String, LocalizedText> =
    pinnedRepos.mapNotNull { setting ->
        val ja = setting.descriptionJa.ifBlank { setting.descriptionEn }
        val en = setting.descriptionEn.ifBlank { ja }
        if (ja.isBlank()) null else setting.name to LocalizedText(ja = ja, en = en)
    }.toMap()

internal fun PublishedProfile.hidesPinnedRepo(name: String): Boolean =
    pinnedRepos.any { it.name == name && !it.visible }

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

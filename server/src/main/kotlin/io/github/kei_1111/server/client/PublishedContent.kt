package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.MarkdownListItem
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.TerminalTextCommand
import io.github.kei_1111.shared.model.TerminalTextCommands
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.WorkTag
import io.github.kei_1111.shared.model.Works
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.SerialName
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
    val iconUrl: String = "",
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
    iconUrl = iconUrl.ifBlank { null }?.let { resolveAssetUrl(it, assetBaseUrl) },
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

// admin アップロード規約(images/works/<workId>/<file>・images/profile/<file>)のパスだけ
// 管理サーバー基準の絶対 URL にする。それ以外の相対パスはポートフォリオ同梱資産として据え置く(契約: Work KDoc)
private val adminUploadedAssetPattern = Regex("^images/(?:works/[^/]+|profile)/.+")

internal fun resolveAssetUrl(path: String, assetBaseUrl: String): String = when {
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
    val avatarUrl: String = "",
    val pinnedRepos: List<PublishedPinnedRepo> = emptyList(),
    val socialLinks: List<PublishedSocialLink> = emptyList(),
)

@Serializable
data class PublishedPinnedRepo(
    val name: String,
    val visible: Boolean = true,
    val descriptionJa: String = "",
    val descriptionEn: String = "",
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

/** 管理コンソールの README 公開スキーマ(admin 側の ReadmeContent)。契約モデルとは判別子名が異なる。 */
@Serializable
data class PublishedReadme(
    val ja: List<PublishedReadmeBlock> = emptyList(),
    val en: List<PublishedReadmeBlock> = emptyList(),
)

@Serializable
sealed interface PublishedReadmeBlock {
    @Serializable
    @SerialName("heading")
    data class Heading(val level: Int, val inlines: List<PublishedReadmeInline> = emptyList()) : PublishedReadmeBlock

    @Serializable
    @SerialName("paragraph")
    data class Paragraph(val inlines: List<PublishedReadmeInline> = emptyList()) : PublishedReadmeBlock

    @Serializable
    @SerialName("bulletList")
    data class BulletList(val items: List<List<PublishedReadmeInline>> = emptyList()) : PublishedReadmeBlock
}

@Serializable
sealed interface PublishedReadmeInline {
    @Serializable
    @SerialName("text")
    data class PlainText(val text: String) : PublishedReadmeInline

    @Serializable
    @SerialName("code")
    data class InlineCode(val text: String) : PublishedReadmeInline

    @Serializable
    @SerialName("link")
    data class Link(val text: String, val url: String) : PublishedReadmeInline
}

fun PublishedReadme.toReadme(): Readme = Readme(
    ja = ja.map { it.toBlock() }.toImmutableList(),
    en = en.map { it.toBlock() }.toImmutableList(),
)

private fun PublishedReadmeBlock.toBlock(): MarkdownBlock = when (this) {
    is PublishedReadmeBlock.Heading ->
        MarkdownBlock.Heading(level = level, inlines = inlines.map { it.toInline() }.toImmutableList())

    is PublishedReadmeBlock.Paragraph ->
        MarkdownBlock.Paragraph(inlines = inlines.map { it.toInline() }.toImmutableList())

    is PublishedReadmeBlock.BulletList -> MarkdownBlock.BulletList(
        items = items
            .map { item -> MarkdownListItem(inlines = item.map { it.toInline() }.toImmutableList()) }
            .toImmutableList(),
    )
}

private fun PublishedReadmeInline.toInline(): MarkdownInline = when (this) {
    is PublishedReadmeInline.PlainText -> MarkdownInline.PlainText(text)
    is PublishedReadmeInline.InlineCode -> MarkdownInline.InlineCode(text)
    is PublishedReadmeInline.Link -> MarkdownInline.Link(text = text, url = url)
}

/** 管理コンソールのターミナルコマンド公開スキーマ。 */
@Serializable
data class PublishedTerminalCommands(
    val commands: List<PublishedTerminalCommand> = emptyList(),
)

@Serializable
data class PublishedTerminalCommand(
    val keyword: String,
    val description: String = "",
    val lines: List<String> = emptyList(),
)

/** キーワード未入力のカードは配信対象から除く。 */
fun PublishedTerminalCommands.toTerminalTextCommands(): TerminalTextCommands = TerminalTextCommands(
    items = commands
        .filter { it.keyword.isNotBlank() }
        .map { TerminalTextCommand(keyword = it.keyword, description = it.description, lines = it.lines.toImmutableList()) }
        .toImmutableList(),
)

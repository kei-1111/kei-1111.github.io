package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.MarkdownListItem
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
internal data class PublishedWorks(
    val works: List<PublishedWork> = emptyList(),
)

@Serializable
internal data class PublishedWork(
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

internal fun PublishedWorks.toWorks(assetBaseUrl: String): Works = Works(
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
    storeUrl = httpUrlOrNull(googlePlayUrl),
    sourceUrl = httpUrlOrNull(sourceUrl),
)

// 公開 URL はクライアントが window.open で開くため、http(s) 以外(javascript: 等)は配信しない
internal fun httpUrlOrNull(url: String): String? =
    url.takeIf { it.startsWith("http://") || it.startsWith("https://") }

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
internal fun localized(ja: String, en: String): LocalizedText =
    LocalizedText(ja = ja, en = en.ifBlank { ja })

/** 管理コンソールの README 公開スキーマ(admin 側の ReadmeContent)。契約モデルとは判別子名が異なる。 */
@Serializable
internal data class PublishedReadme(
    val ja: List<PublishedReadmeBlock> = emptyList(),
    val en: List<PublishedReadmeBlock> = emptyList(),
)

@Serializable
internal sealed interface PublishedReadmeBlock {
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
internal sealed interface PublishedReadmeInline {
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

internal fun PublishedReadme.toReadme(): Readme = Readme(
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
    is PublishedReadmeInline.Link ->
        httpUrlOrNull(url)
            ?.let { MarkdownInline.Link(text = text, url = it) }
            ?: MarkdownInline.PlainText(text)
}

@Serializable
internal data class PublishedTerminalCommands(
    val commands: List<PublishedTerminalCommand> = emptyList(),
)

@Serializable
internal data class PublishedTerminalCommand(
    val keyword: String,
    val description: String = "",
    val lines: List<String> = emptyList(),
)

internal fun PublishedTerminalCommands.toTerminalTextCommands(): TerminalTextCommands = TerminalTextCommands(
    items = commands
        .filter { it.keyword.isNotBlank() }
        .map { TerminalTextCommand(keyword = it.keyword, description = it.description, lines = it.lines.toImmutableList()) }
        .toImmutableList(),
)

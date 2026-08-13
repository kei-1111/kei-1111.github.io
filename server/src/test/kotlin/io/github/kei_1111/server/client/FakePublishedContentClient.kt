package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.MarkdownListItem
import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.TerminalTextCommand
import io.github.kei_1111.shared.model.TerminalTextCommands
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.Works
import kotlinx.collections.immutable.persistentListOf

internal class FakePublishedContentClient(
    private val works: PublishedResult<Works>? = PublishedResult.Missing,
    private val profile: PublishedResult<PublishedProfile>? = PublishedResult.Missing,
    private val readme: PublishedResult<Readme>? = PublishedResult.Missing,
    private val terminalCommands: PublishedResult<TerminalTextCommands>? = PublishedResult.Missing,
) : PublishedContentClient {
    override suspend fun fetchWorks(): PublishedResult<Works>? = works
    override suspend fun fetchProfile(): PublishedResult<PublishedProfile>? = profile
    override suspend fun fetchReadme(): PublishedResult<Readme>? = readme
    override suspend fun fetchTerminalCommands(): PublishedResult<TerminalTextCommands>? = terminalCommands
}

/**
 * ルート単位のテストが 200 を得るための公開コンテンツ一式。
 * ワイヤ形状の固定テストが全フィールドの出力を確認できるよう、既定値と異なる値で全項目を埋める。
 */
internal fun publishedContentClient() = FakePublishedContentClient(
    works = PublishedResult.Found(PublishedWorksFixture),
    profile = PublishedResult.Found(PublishedProfileFixture),
    readme = PublishedResult.Found(PublishedReadmeFixture),
    terminalCommands = PublishedResult.Found(PublishedTerminalCommandsFixture),
)

internal val PublishedProfileFixture = PublishedProfile(
    displayName = "けい",
    displayNameEn = "Kei",
    role = "Student Developer",
    location = "Tokyo",
    avatarUrl = "https://admin.example/images/profile/avatar.webp",
    socialLinks = listOf(PublishedSocialLink(service = "GitHub", url = "https://github.com/kei-1111")),
    pinnedRepos = listOf(
        PublishedPinnedRepo(
            name = "kei-1111.github.io",
            descriptionJa = "公開側の説明",
            descriptionEn = "Published description",
        ),
    ),
)

internal val PublishedWorksFixture = Works(
    items = persistentListOf(
        Work(
            id = "published-work",
            name = "Published Work",
            kind = "Web App",
            period = "2026–",
            description = LocalizedText(ja = "公開された作品", en = "Published work"),
        ),
    ),
)

/** 見出し・段落・箇条書き2種を含め、README のワイヤ形状を一通り通す。 */
internal val PublishedReadmeFixture = Readme(
    ja = persistentListOf(
        MarkdownBlock.Heading(level = 1, inlines = persistentListOf(MarkdownInline.PlainText("kei-1111.github.io"))),
        MarkdownBlock.Paragraph(inlines = persistentListOf(MarkdownInline.PlainText("公開された本文"))),
        MarkdownBlock.BulletList(
            items = persistentListOf(
                MarkdownListItem(inlines = persistentListOf(MarkdownInline.PlainText("箇条書き1"))),
            ),
        ),
        MarkdownBlock.BulletList(
            items = persistentListOf(
                MarkdownListItem(inlines = persistentListOf(MarkdownInline.InlineCode("./gradlew build"))),
            ),
        ),
    ),
    en = persistentListOf(
        MarkdownBlock.Heading(level = 1, inlines = persistentListOf(MarkdownInline.PlainText("kei-1111.github.io"))),
    ),
)

internal val PublishedTerminalCommandsFixture = TerminalTextCommands(
    items = persistentListOf(
        TerminalTextCommand(
            keyword = "neofetch",
            description = "show portfolio system info",
            lines = persistentListOf("kei@kei-1111.github.io"),
        ),
        TerminalTextCommand(
            keyword = "sudo",
            description = "run a command as another user",
            lines = persistentListOf("kei is not in the sudoers file. This incident will be reported."),
        ),
    ),
)

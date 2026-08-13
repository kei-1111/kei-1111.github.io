package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.TerminalTextCommands
import io.github.kei_1111.shared.model.Works

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
 * ルート単位のテストで /api/profile を 200 にするための公開プロフィール。
 * ワイヤ形状の固定テストが全フィールドの出力を確認できるよう、既定値と異なる値で全項目を埋める。
 */
internal fun publishedProfileClient() = FakePublishedContentClient(
    profile = PublishedResult.Found(
        PublishedProfile(
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
        ),
    ),
)

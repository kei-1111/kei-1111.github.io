package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.PinnedRepo
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class PublishedProfileOverlayTest {

    private val base = GitHubProfile(
        name = LocalizedText(ja = "けい", en = "Kei"),
        handle = "kei-1111",
        location = "Tokyo",
        role = "Student Developer",
        followers = 10,
        following = 5,
        repos = 20,
        totalStars = 30,
        pinnedRepos = persistentListOf(
            PinnedRepo(name = "withmo", description = LocalizedText("a", "a"), url = "https://github.com/withmo"),
            PinnedRepo(name = "portfolio", description = LocalizedText("b", "b"), url = "https://github.com/p"),
        ),
        languages = persistentListOf(),
        links = persistentListOf(
            LinkService(type = LinkServiceType.GitHub, name = "GitHub", url = "https://github.com/kei-1111"),
        ),
    )

    @Test
    fun overlaysEditableFieldsAndKeepsGitHubStats() {
        val published = PublishedProfile(
            displayName = "新しい名前",
            displayNameEn = "",
            role = "Android Engineer",
            location = "",
            socialLinks = listOf(
                PublishedSocialLink(service = "GitHub", url = "https://github.com/kei-1111"),
                PublishedSocialLink(service = "X", url = "https://x.com/kei"),
                PublishedSocialLink(service = "unknown-service", url = "https://example.com"),
            ),
            pinnedRepos = listOf(
                PublishedPinnedRepo(name = "withmo", visible = false),
                PublishedPinnedRepo(name = "portfolio", visible = true),
            ),
        )

        val overlaid = published.overlayOn(base)

        // 表示名は ja 上書き + en 空なら ja フォールバック
        assertEquals(LocalizedText(ja = "新しい名前", en = "新しい名前"), overlaid.name)
        assertEquals("Android Engineer", overlaid.role)
        // 公開値は空でも authoritative(管理画面 Preview と本番を一致させる)
        assertEquals("", overlaid.location)
        // 統計はベースのまま
        assertEquals(10, overlaid.followers)
        assertEquals(30, overlaid.totalStars)
        // 変換可能なリンクだけ置き換え(unknown は落とす)
        assertEquals(
            listOf(
                LinkService(type = LinkServiceType.GitHub, name = "GitHub", url = "https://github.com/kei-1111"),
                LinkService(type = LinkServiceType.X, name = "X", url = "https://x.com/kei"),
            ),
            overlaid.links.toList(),
        )
        // 非表示指定の pinned は除外
        assertEquals(listOf("portfolio"), overlaid.pinnedRepos.map { it.name })
    }

    @Test
    fun clearingAllSocialLinksInThePublishedProfileClearsThemInProduction() {
        val published = PublishedProfile(displayName = "けい", socialLinks = emptyList())

        val overlaid = published.overlayOn(base)

        // 最後のリンクを消して公開したらベースのリンクが復活してはいけない
        assertEquals(emptyList(), overlaid.links.toList())
        assertEquals(base.pinnedRepos, overlaid.pinnedRepos)
    }

    @Test
    fun legacyXUrlBecomesAnXLinkWhenSocialLinksLackOne() {
        val published = PublishedProfile(
            displayName = "けい",
            xUrl = "https://x.com/legacy",
            socialLinks = listOf(PublishedSocialLink(service = "GitHub", url = "https://github.com/kei-1111")),
        )

        val overlaid = published.overlayOn(base)

        assertEquals(
            listOf(LinkServiceType.GitHub, LinkServiceType.X),
            overlaid.links.map { it.type },
        )
        assertEquals("https://x.com/legacy", overlaid.links.last().url)
    }
}

package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.WorkTag
import kotlin.test.Test
import kotlin.test.assertEquals

class PublishedContentMappingTest {

    @Test
    fun mapsAPublishedWorkToTheContractModel() {
        val published = PublishedWorks(
            works = listOf(
                PublishedWork(
                    id = "withmo",
                    name = "withmo",
                    type = "Android Launcher App",
                    period = "2024–",
                    about = "日本語説明",
                    aboutEn = "English description",
                    roles = listOf("役割A", "役割B"),
                    rolesEn = listOf("Role A"),
                    googlePlayUrl = "https://play.example/withmo",
                    sourceUrl = "",
                ),
            ),
        )

        val works = published.toWorks(assetBaseUrl = "https://admin.example")

        val work = works.items.single()
        assertEquals("withmo", work.id)
        assertEquals("Android Launcher App", work.kind)
        assertEquals("2024–", work.period)
        assertEquals(LocalizedText(ja = "日本語説明", en = "English description"), work.description)
        // rolesEn が足りない index は ja へフォールバック
        assertEquals(
            listOf(
                LocalizedText(ja = "役割A", en = "Role A"),
                LocalizedText(ja = "役割B", en = "役割B"),
            ),
            work.roles.toList(),
        )
        assertEquals("https://play.example/withmo", work.storeUrl)
        assertEquals(null, work.sourceUrl)
        assertEquals(null, work.iconUrl)
    }

    @Test
    fun fallsBackToJapaneseWhenTheEnglishDescriptionIsBlank() {
        val published = PublishedWorks(
            works = listOf(PublishedWork(id = "a", name = "a", about = "説明", aboutEn = " ")),
        )

        val work = published.toWorks(assetBaseUrl = "https://admin.example").items.single()

        assertEquals(LocalizedText(ja = "説明", en = "説明"), work.description)
    }

    @Test
    fun marksLanguageAndUiTagsAsAccent() {
        val published = PublishedWorks(
            works = listOf(
                PublishedWork(
                    id = "a",
                    name = "a",
                    techStack = listOf("Kotlin", "Jetpack Compose", "GitHub Actions", "detekt"),
                ),
            ),
        )

        val tags = published.toWorks(assetBaseUrl = "https://admin.example").items.single().tags.toList()

        assertEquals(
            listOf(
                WorkTag(name = "Kotlin", accent = true),
                WorkTag(name = "Jetpack Compose", accent = true),
                WorkTag(name = "GitHub Actions", accent = false),
                WorkTag(name = "detekt", accent = false),
            ),
            tags,
        )
    }

    @Test
    fun resolvesOnlyAdminUploadedScreenshotPathsAgainstTheAssetBase() {
        val published = PublishedWorks(
            works = listOf(
                PublishedWork(
                    id = "a",
                    name = "a",
                    screenshots = listOf(
                        // admin アップロード規約(images/works/<workId>/<file>)→ 絶対 URL 化
                        "images/works/withmo/1723-shot.png",
                        // ポートフォリオ同梱資産(サブディレクトリなし)→ 相対のまま
                        "images/works/withmo-1.webp",
                        // 絶対 URL はそのまま
                        "https://cdn.example/x.png",
                    ),
                ),
            ),
        )

        val screenshots = published.toWorks(assetBaseUrl = "https://admin.example").items.single().screenshots

        assertEquals(
            listOf(
                "https://admin.example/images/works/withmo/1723-shot.png",
                "images/works/withmo-1.webp",
                "https://cdn.example/x.png",
            ),
            screenshots.toList(),
        )
    }

    @Test
    fun resolvesTheWorkIconLikeScreenshots() {
        val published = PublishedWorks(
            works = listOf(
                PublishedWork(id = "a", name = "a", iconUrl = "images/works/a/1-icon.png"),
                PublishedWork(id = "b", name = "b", iconUrl = "images/works/withmo-icon.webp"),
                PublishedWork(id = "c", name = "c"),
            ),
        )

        val works = published.toWorks(assetBaseUrl = "https://admin.example").items

        assertEquals("https://admin.example/images/works/a/1-icon.png", works[0].iconUrl)
        assertEquals("images/works/withmo-icon.webp", works[1].iconUrl)
        assertEquals(null, works[2].iconUrl)
    }

    @Test
    fun passesTheTerminalCommandDescriptionThrough() {
        val published = PublishedTerminalCommands(
            commands = listOf(
                PublishedTerminalCommand(keyword = "help", description = "コマンド一覧を表示", lines = listOf("help text")),
                PublishedTerminalCommand(keyword = "", description = "キーワードなしは除外"),
            ),
        )

        val commands = published.toTerminalTextCommands().items

        assertEquals("コマンド一覧を表示", commands.single().description)
    }
}

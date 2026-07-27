package io.github.kei_1111.app.feature.profile.destination.profile.component

import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownBlock
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownInline.InlineCode
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownInline.Link
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownInline.PlainText
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.markdownSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** サイト内 README.md の日本語コンテンツ。エディタとプレビューの両方がここから導出される。 */
private val ReadmeBlocksJa: ImmutableList<MarkdownBlock> = persistentListOf(
    MarkdownBlock.Heading(
        level = 1,
        inlines = listOf(PlainText("kei-1111.github.io")),
    ),
    MarkdownBlock.Paragraph(
        inlines = listOf(
            PlainText(
                "Android Studio New UI を再現した kei-1111 のポートフォリオサイトです。" +
                    "Kotlin / Compose Multiplatform (Wasm) で実装しています。",
            ),
        ),
    ),
    MarkdownBlock.Heading(
        level = 2,
        inlines = listOf(PlainText("このサイトの歩き方")),
    ),
    MarkdownBlock.BulletList(
        items = listOf(
            listOf(
                PlainText("エディタ上部のタブか Project ツリーの "),
                InlineCode("ProfileScreen.kt"),
                PlainText(" を開くと、プロフィールが表示されます"),
            ),
            listOf(
                PlainText(
                    "コードエディタと Preview ペインは、実際の " +
                        "Android Studio と同じように同じ内容を表示します",
                ),
            ),
            listOf(PlainText("エディタ右上のボタンで Code / Split / Preview の表示モードを切り替えられます")),
            listOf(PlainText("タイトルバー右上のボタンで言語と Dark / Light テーマを切り替えられます")),
        ),
    ),
    MarkdownBlock.Heading(
        level = 2,
        inlines = listOf(PlainText("技術スタック")),
    ),
    MarkdownBlock.BulletList(
        items = listOf(
            listOf(PlainText("Kotlin / Compose Multiplatform (wasmJs)")),
            listOf(PlainText("Ktor + Cloud Run — プロフィールと Contributions を GitHub GraphQL API からライブ取得")),
            listOf(PlainText("GitHub Pages + GitHub Actions による CI/CD")),
        ),
    ),
    MarkdownBlock.Heading(
        level = 2,
        inlines = listOf(PlainText("リポジトリ")),
    ),
    MarkdownBlock.Paragraph(
        inlines = listOf(
            Link(
                text = "kei-1111/kei-1111.github.io",
                url = "https://github.com/kei-1111/kei-1111.github.io",
            ),
        ),
    ),
)

/** サイト内 README.md の英語コンテンツ。[ReadmeBlocksJa] と構造を揃えて対で更新する。 */
private val ReadmeBlocksEn: ImmutableList<MarkdownBlock> = persistentListOf(
    MarkdownBlock.Heading(
        level = 1,
        inlines = listOf(PlainText("kei-1111.github.io")),
    ),
    MarkdownBlock.Paragraph(
        inlines = listOf(
            PlainText(
                "kei-1111's portfolio site that mimics the Android Studio New UI. " +
                    "Built with Kotlin / Compose Multiplatform (Wasm).",
            ),
        ),
    ),
    MarkdownBlock.Heading(
        level = 2,
        inlines = listOf(PlainText("How to explore this site")),
    ),
    MarkdownBlock.BulletList(
        items = listOf(
            listOf(
                PlainText("Open "),
                InlineCode("ProfileScreen.kt"),
                PlainText(" from the editor tabs or the Project tree to show the profile"),
            ),
            listOf(
                PlainText(
                    "The code editor and the Preview pane always show the same content, " +
                        "just like the real Android Studio",
                ),
            ),
            listOf(PlainText("The buttons at the top right of the editor switch the Code / Split / Preview view mode")),
            listOf(PlainText("The buttons at the top right of the title bar switch the language and the Dark / Light theme")),
        ),
    ),
    MarkdownBlock.Heading(
        level = 2,
        inlines = listOf(PlainText("Tech stack")),
    ),
    MarkdownBlock.BulletList(
        items = listOf(
            listOf(PlainText("Kotlin / Compose Multiplatform (wasmJs)")),
            listOf(PlainText("Ktor + Cloud Run — fetches the profile and Contributions live from the GitHub GraphQL API")),
            listOf(PlainText("CI/CD with GitHub Pages + GitHub Actions")),
        ),
    ),
    MarkdownBlock.Heading(
        level = 2,
        inlines = listOf(PlainText("Repository")),
    ),
    MarkdownBlock.Paragraph(
        inlines = listOf(
            Link(
                text = "kei-1111/kei-1111.github.io",
                url = "https://github.com/kei-1111/kei-1111.github.io",
            ),
        ),
    ),
)

internal fun readmeBlocks(language: KeiLanguage): ImmutableList<MarkdownBlock> = when (language) {
    KeiLanguage.Ja -> ReadmeBlocksJa
    KeiLanguage.En -> ReadmeBlocksEn
}

/** 各言語の [readmeBlocks] から生成した Markdown ソース。静的な内容のため言語ごとに1回だけ評価する。 */
private val ReadmeSourceJa: String = markdownSource(ReadmeBlocksJa)
private val ReadmeSourceEn: String = markdownSource(ReadmeBlocksEn)

internal fun readmeSource(language: KeiLanguage): String = when (language) {
    KeiLanguage.Ja -> ReadmeSourceJa
    KeiLanguage.En -> ReadmeSourceEn
}

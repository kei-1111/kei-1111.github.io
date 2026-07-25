package io.github.kei_1111.app.feature.profile.destination.profile.component

import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownBlock
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownInline.InlineCode
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownInline.Link
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownInline.PlainText
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.markdownSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** サイト内 README.md のコンテンツ。エディタとプレビューの両方がここから導出される。 */
internal val ReadmeBlocks: ImmutableList<MarkdownBlock> = persistentListOf(
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
            listOf(PlainText("タイトルバー右上のボタンで Dark / Light テーマを切り替えられます")),
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

/** [ReadmeBlocks] から生成した Markdown ソース。静的な内容のため1回だけ評価する。 */
internal val ReadmeSource: String = markdownSource(ReadmeBlocks)

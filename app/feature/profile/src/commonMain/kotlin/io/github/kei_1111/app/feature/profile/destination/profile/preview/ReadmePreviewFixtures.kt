package io.github.kei_1111.app.feature.profile.destination.profile.preview

import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline.InlineCode
import io.github.kei_1111.shared.model.MarkdownInline.Link
import io.github.kei_1111.shared.model.MarkdownInline.PlainText
import io.github.kei_1111.shared.model.MarkdownListItem
import io.github.kei_1111.shared.model.Readme
import kotlinx.collections.immutable.persistentListOf

// server/.../content/ReadmeContent.kt（DefaultReadme）の複製。feature モジュールは app:core:data に
// 依存できないため、実データ変更時は両方を揃えて編集する（WorksPreviewFixtures と同じ運用）。
internal val PreviewReadme = Readme(
    ja = persistentListOf(
        MarkdownBlock.Heading(
            level = 1,
            inlines = persistentListOf(PlainText("kei-1111.github.io")),
        ),
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(
                PlainText(
                    "Android Studio New UI を再現した kei-1111 のポートフォリオサイトです。" +
                        "Kotlin / Compose Multiplatform (Wasm) で実装しています。",
                ),
            ),
        ),
        MarkdownBlock.Heading(
            level = 2,
            inlines = persistentListOf(PlainText("このサイトの歩き方")),
        ),
        MarkdownBlock.BulletList(
            items = persistentListOf(
                MarkdownListItem(
                    persistentListOf(
                        PlainText("エディタ上部のタブか Project ツリーの "),
                        InlineCode("ProfileScreen.kt"),
                        PlainText(" を開くと、プロフィールが表示されます"),
                    ),
                ),
                MarkdownListItem(
                    persistentListOf(
                        PlainText(
                            "コードエディタと Preview ペインは、実際の " +
                                "Android Studio と同じように同じ内容を表示します",
                        ),
                    ),
                ),
                MarkdownListItem(
                    persistentListOf(
                        PlainText("エディタ右上のボタンで Code / Split / Preview の表示モードを切り替えられます"),
                    ),
                ),
                MarkdownListItem(
                    persistentListOf(
                        PlainText("タイトルバー右上のボタンで言語と Dark / Light テーマを切り替えられます"),
                    ),
                ),
            ),
        ),
        MarkdownBlock.Heading(
            level = 2,
            inlines = persistentListOf(PlainText("技術スタック")),
        ),
        MarkdownBlock.BulletList(
            items = persistentListOf(
                MarkdownListItem(persistentListOf(PlainText("Kotlin / Compose Multiplatform (wasmJs)"))),
                MarkdownListItem(
                    persistentListOf(
                        PlainText("Ktor + Cloud Run — プロフィールと Contributions を GitHub GraphQL API からライブ取得"),
                    ),
                ),
                MarkdownListItem(persistentListOf(PlainText("GitHub Pages + GitHub Actions による CI/CD"))),
            ),
        ),
        MarkdownBlock.Heading(
            level = 2,
            inlines = persistentListOf(PlainText("リポジトリ")),
        ),
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(
                Link(
                    text = "kei-1111/kei-1111.github.io",
                    url = "https://github.com/kei-1111/kei-1111.github.io",
                ),
            ),
        ),
    ),
    en = persistentListOf(
        MarkdownBlock.Heading(
            level = 1,
            inlines = persistentListOf(PlainText("kei-1111.github.io")),
        ),
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(
                PlainText(
                    "kei-1111's portfolio site that mimics the Android Studio New UI. " +
                        "Built with Kotlin / Compose Multiplatform (Wasm).",
                ),
            ),
        ),
        MarkdownBlock.Heading(
            level = 2,
            inlines = persistentListOf(PlainText("How to explore this site")),
        ),
        MarkdownBlock.BulletList(
            items = persistentListOf(
                MarkdownListItem(
                    persistentListOf(
                        PlainText("Open "),
                        InlineCode("ProfileScreen.kt"),
                        PlainText(" from the editor tabs or the Project tree to show the profile"),
                    ),
                ),
                MarkdownListItem(
                    persistentListOf(
                        PlainText(
                            "The code editor and the Preview pane always show the same content, " +
                                "just like the real Android Studio",
                        ),
                    ),
                ),
                MarkdownListItem(
                    persistentListOf(
                        PlainText(
                            "The buttons at the top right of the editor switch the Code / Split / Preview view mode",
                        ),
                    ),
                ),
                MarkdownListItem(
                    persistentListOf(
                        PlainText(
                            "The buttons at the top right of the title bar switch the language and the Dark / Light theme",
                        ),
                    ),
                ),
            ),
        ),
        MarkdownBlock.Heading(
            level = 2,
            inlines = persistentListOf(PlainText("Tech stack")),
        ),
        MarkdownBlock.BulletList(
            items = persistentListOf(
                MarkdownListItem(persistentListOf(PlainText("Kotlin / Compose Multiplatform (wasmJs)"))),
                MarkdownListItem(
                    persistentListOf(
                        PlainText(
                            "Ktor + Cloud Run — fetches the profile and Contributions live from the GitHub GraphQL API",
                        ),
                    ),
                ),
                MarkdownListItem(persistentListOf(PlainText("CI/CD with GitHub Pages + GitHub Actions"))),
            ),
        ),
        MarkdownBlock.Heading(
            level = 2,
            inlines = persistentListOf(PlainText("Repository")),
        ),
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(
                Link(
                    text = "kei-1111/kei-1111.github.io",
                    url = "https://github.com/kei-1111/kei-1111.github.io",
                ),
            ),
        ),
    ),
)

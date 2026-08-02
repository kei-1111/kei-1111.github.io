package io.github.kei_1111.server.content

import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Work
import kotlinx.collections.immutable.persistentListOf

/** 配信する作品一覧。GitHub API に依存しない静的コンテンツ。 */
internal val DefaultWorks = listOf(
    Work(
        id = "withmo",
        name = "withmo",
        stack = "Kotlin · Jetpack Compose · Unity",
        description = LocalizedText(
            ja = "デジタルフィギュア × ランチャーがコンセプトの Android ランチャーアプリ。" +
                "お気に入りの3Dモデルと一緒にホーム画面をカスタマイズできる。",
            en = "An Android launcher app built on the digital figure × launcher concept. " +
                "Customize your home screen together with your favorite 3D model.",
        ),
        tags = persistentListOf("Jetpack Compose", "Unity as a Library", "GitHub Actions", "detekt"),
        iconUrl = "https://kei-1111.github.io/images/works/withmo-icon.webp",
        screenshots = persistentListOf(
            "https://kei-1111.github.io/images/works/withmo-1.webp",
            "https://kei-1111.github.io/images/works/withmo-2.webp",
            "https://kei-1111.github.io/images/works/withmo-3.webp",
        ),
        storeUrl = "https://play.google.com/store/apps/details?id=io.github.kei_1111.withmo",
        sourceUrl = null,
    ),
    Work(
        id = "kei-1111-github-io",
        name = "kei-1111.github.io",
        stack = "Kotlin · Compose Multiplatform",
        description = LocalizedText(
            ja = "Android Studio の UI を再現したポートフォリオサイト。" +
                "Compose Multiplatform を WebAssembly にコンパイルし GitHub Pages で配信。",
            en = "A portfolio site that recreates the Android Studio UI, built with Compose Multiplatform " +
                "compiled to WebAssembly and served on GitHub Pages.",
        ),
        tags = persistentListOf("Compose Multiplatform", "Kotlin/Wasm", "Ktor", "Cloud Run", "Playwright"),
        iconUrl = "https://kei-1111.github.io/images/works/portfolio-icon.webp",
        screenshots = persistentListOf(
            "https://kei-1111.github.io/images/works/portfolio-1.webp",
            "https://kei-1111.github.io/images/works/portfolio-2.webp",
        ),
        storeUrl = null,
        sourceUrl = "https://github.com/kei-1111/kei-1111.github.io",
    ),
)

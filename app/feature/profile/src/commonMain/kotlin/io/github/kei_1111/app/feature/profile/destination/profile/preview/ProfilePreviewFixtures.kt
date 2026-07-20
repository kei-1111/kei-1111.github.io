@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.preview

import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.ContributionDay
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.LicenseType
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.RepoLanguage
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Preview 専用の GitHub プロフィールデータ。
 * feature は :core:data に依存できない（レイヤリングルール）ため、
 * github.com/kei-1111 の実データを Preview 用に複製している。
 */
internal val PreviewGitHubProfile = GitHubProfile(
    name = "けい",
    handle = "kei-1111",
    location = "Japan",
    role = "Android developer",
    followers = 15,
    following = 25,
    repos = 32,
    totalStars = 41,
    pinnedRepos = persistentListOf(
        PinnedRepo(
            name = "kei-1111.github.io",
            description = "自己紹介Webサイトのリポジトリ",
            url = "https://github.com/kei-1111/kei-1111.github.io",
            language = RepoLanguage.Kotlin,
        ),
        PinnedRepo(
            name = "android-template",
            description = "My Android Template Project",
            url = "https://github.com/kei-1111/android-template",
            stars = 2,
        ),
        PinnedRepo(
            name = "kmp-sample-library",
            description = "KMP Library のサンプルリポジトリ",
            url = "https://github.com/kei-1111/kmp-sample-library",
            language = RepoLanguage.Kotlin,
        ),
        PinnedRepo(
            name = "kmp-sample-ios",
            description = "KMPライブラリを使うiOSアプリ",
            url = "https://github.com/kei-1111/kmp-sample-ios",
            language = RepoLanguage.Swift,
        ),
    ),
    languages = persistentListOf(
        LanguageShare(language = RepoLanguage.Kotlin, share = 0.78f),
        LanguageShare(language = RepoLanguage.Swift, share = 0.12f),
        LanguageShare(language = RepoLanguage.Shell, share = 0.10f),
    ),
    links = persistentListOf(
        LinkService(type = LinkServiceType.GitHub, name = "GitHub", url = "https://github.com/kei-1111"),
        LinkService(type = LinkServiceType.X, name = "X", url = "https://x.com/kei_1111_"),
        LinkService(type = LinkServiceType.Qiita, name = "Qiita", url = "https://qiita.com/kei-1111"),
        LinkService(type = LinkServiceType.Note, name = "note", url = "https://note.com/kei_1111_"),
    ),
)

/** Preview 専用のコントリビューションカレンダー（実データではなくサンプル値）。 */
internal val PreviewContributionCalendar = ContributionCalendar(
    totalLastYear = 620,
    days = List(53 * 7) { index ->
        val level = index % 5
        ContributionDay(
            date = "2026-01-${(index % 28 + 1).toString().padStart(2, '0')}",
            count = level * 3,
            level = level,
        )
    }.toImmutableList(),
)

/**
 * Preview 専用のサードパーティライセンスデータ。エントリは実データ（[io.github.kei_1111.app.core.data.license.LicenseContent]）
 * と同じものを複製しているが、texts は全文を持たず各ライセンスの冒頭数行のみに短縮している。
 */
internal val PreviewThirdPartyLicenses = ThirdPartyLicenses(
    icons = persistentListOf(
        LicenseEntry(
            name = "IntelliJ Platform Icons",
            owner = "JetBrains s.r.o.",
            type = LicenseType.Apache20,
            url = "https://github.com/JetBrains/intellij-community",
            copyright = "Copyright JetBrains s.r.o. and/or its affiliates.",
        ),
        LicenseEntry(
            name = "Android Studio New UI Icons",
            owner = "Google LLC",
            type = LicenseType.Apache20,
            url = "https://developer.android.com/studio",
            copyright = "Copyright Google LLC.",
        ),
    ),
    fonts = persistentListOf(
        LicenseEntry(
            name = "JetBrains Mono",
            owner = "JetBrains s.r.o.",
            type = LicenseType.Ofl11,
            url = "https://github.com/JetBrains/JetBrainsMono",
            copyright = "Copyright 2020 The JetBrains Mono Project Authors " +
                "(https://github.com/JetBrains/JetBrainsMono)",
        ),
        LicenseEntry(
            name = "Noto Sans JP",
            owner = "Google / Adobe",
            type = LicenseType.Ofl11,
            url = "https://fonts.google.com/noto/specimen/Noto+Sans+JP",
            copyright = "(c) 2014-2021 Adobe (http://www.adobe.com/), with Reserved Font Name 'Source'.",
        ),
        LicenseEntry(
            name = "Zen Kaku Gothic New",
            owner = "Font Zen Project",
            type = LicenseType.Ofl11,
            url = "https://github.com/googlefonts/zen-kakugothic",
            copyright = "Copyright 2022 The Zen Project Authors (https://github.com/googlefonts/zen-kakugothic)",
        ),
    ),
    app = persistentListOf(
        LicenseEntry(
            name = "Kotlin Standard Library",
            owner = "org.jetbrains.kotlin",
            type = LicenseType.Apache20,
            url = "https://github.com/JetBrains/kotlin",
            copyright = "Copyright JetBrains s.r.o. and Kotlin Programming Language " +
                "contributors.",
        ),
        LicenseEntry(
            name = "Compose Multiplatform",
            owner = "org.jetbrains.compose",
            type = LicenseType.Apache20,
            url = "https://github.com/JetBrains/compose-multiplatform",
            copyright = "Copyright JetBrains s.r.o. and respective authors and " +
                "developers.",
        ),
        LicenseEntry(
            name = "Skiko",
            owner = "org.jetbrains.skiko",
            type = LicenseType.Apache20,
            url = "https://github.com/JetBrains/skiko",
            copyright = "Copyright JetBrains s.r.o.",
        ),
        LicenseEntry(
            name = "kotlinx.coroutines",
            owner = "org.jetbrains.kotlinx",
            type = LicenseType.Apache20,
            url = "https://github.com/Kotlin/kotlinx.coroutines",
            copyright = "Copyright JetBrains s.r.o. and Kotlin Programming Language " +
                "contributors.",
        ),
        LicenseEntry(
            name = "kotlinx.serialization",
            owner = "org.jetbrains.kotlinx",
            type = LicenseType.Apache20,
            url = "https://github.com/Kotlin/kotlinx.serialization",
            copyright = "Copyright JetBrains s.r.o. and respective authors and " +
                "developers.",
        ),
        LicenseEntry(
            name = "kotlinx-collections-immutable",
            owner = "org.jetbrains.kotlinx",
            type = LicenseType.Apache20,
            url = "https://github.com/Kotlin/kotlinx.collections.immutable",
            copyright = "Copyright JetBrains s.r.o. and respective authors and " +
                "developers.",
        ),
        LicenseEntry(
            name = "kotlinx-browser",
            owner = "org.jetbrains.kotlinx",
            type = LicenseType.Apache20,
            url = "https://github.com/Kotlin/kotlinx-browser",
            copyright = "Copyright JetBrains s.r.o. and respective authors and " +
                "developers.",
        ),
        LicenseEntry(
            name = "AndroidX Lifecycle",
            owner = "org.jetbrains.androidx.lifecycle",
            type = LicenseType.Apache20,
            url = "https://developer.android.com/jetpack/androidx/releases/lifecycle",
            copyright = "Copyright The Android Open Source Project",
        ),
        LicenseEntry(
            name = "AndroidX Navigation 3",
            owner = "androidx.navigation3",
            type = LicenseType.Apache20,
            url = "https://developer.android.com/jetpack/androidx/releases/navigation3",
            copyright = "Copyright The Android Open Source Project",
        ),
        LicenseEntry(
            name = "AndroidX DataStore",
            owner = "androidx.datastore",
            type = LicenseType.Apache20,
            url = "https://developer.android.com/jetpack/androidx/releases/datastore",
            copyright = "Copyright The Android Open Source Project",
        ),
        LicenseEntry(
            name = "Metro",
            owner = "dev.zacsweers.metro",
            type = LicenseType.Apache20,
            url = "https://github.com/ZacSweers/metro",
            copyright = "Copyright Zac Sweers.",
        ),
    ),
    server = persistentListOf(
        LicenseEntry(
            name = "Ktor",
            owner = "io.ktor",
            type = LicenseType.Apache20,
            url = "https://github.com/ktorio/ktor",
            copyright = "Copyright JetBrains s.r.o. and contributors.",
        ),
        LicenseEntry(
            name = "Logback Classic",
            owner = "ch.qos.logback",
            type = LicenseType.Epl10,
            url = "https://logback.qos.ch/",
            copyright = "Copyright (C) QOS.ch. All rights reserved. Dual-licensed " +
                "under the EPL v1.0 and the LGPL 2.1.",
        ),
    ),
    // Preview 用に全文は持たない
    texts = persistentMapOf(
        LicenseType.Apache20 to """
            Apache License
            Version 2.0, January 2004
            http://www.apache.org/licenses/

            TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION
        """.trimIndent(),
        LicenseType.Ofl11 to """
            This Font Software is licensed under the SIL Open Font License, Version 1.1.
            This license is copied below, and is also available with a FAQ at:
            https://openfontlicense.org
        """.trimIndent(),
        LicenseType.Epl10 to """
            Eclipse Public License - v 1.0

            THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS ECLIPSE PUBLIC
            LICENSE ("AGREEMENT").
        """.trimIndent(),
    ),
)

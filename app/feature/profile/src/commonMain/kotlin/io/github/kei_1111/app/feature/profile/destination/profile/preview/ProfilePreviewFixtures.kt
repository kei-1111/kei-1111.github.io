@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.preview

import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.ContributionDay
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubIssue
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.GitHubPullRequest
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.LicenseType
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.Profile
import io.github.kei_1111.shared.model.RepoLanguage
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

/** Preview 専用のマージ済み PR 一覧。実リポジトリの PR のスナップショット。 */
internal val PreviewGitHubChangelog = GitHubChangelog(
    pullRequests = persistentListOf(
        GitHubPullRequest(
            number = 204,
            title = "Introduce KeiAsyncImage and migrate image call sites",
            url = "https://github.com/kei-1111/kei-1111.github.io/pull/204",
            headRefName = "refactor/#201",
            mergedAt = "2026-08-09T06:02:11Z",
            type = "Refactor",
            author = "kei-1111",
        ),
        GitHubPullRequest(
            number = 203,
            title = "Fetch pinned repositories live with description overrides",
            url = "https://github.com/kei-1111/kei-1111.github.io/pull/203",
            headRefName = "feature/#195",
            mergedAt = "2026-08-09T05:58:44Z",
            type = "Feature",
            author = "kei-1111",
        ),
        GitHubPullRequest(
            number = 202,
            title = "Split terminal commands into Action and Text hierarchies",
            url = "https://github.com/kei-1111/kei-1111.github.io/pull/202",
            headRefName = "feature/#193",
            mergedAt = "2026-08-09T05:54:03Z",
            type = "Feature",
            author = "kei-1111",
        ),
        GitHubPullRequest(
            number = 199,
            title = "Aggregate language shares from live repository data",
            url = "https://github.com/kei-1111/kei-1111.github.io/pull/199",
            headRefName = "feature/#194",
            mergedAt = "2026-08-08T11:32:19Z",
            type = "Feature",
            author = "kei-1111",
        ),
        GitHubPullRequest(
            number = 190,
            title = "Make the GitHub preview card content scrollable",
            url = "https://github.com/kei-1111/kei-1111.github.io/pull/190",
            headRefName = "fix/#189",
            mergedAt = "2026-08-07T09:15:40Z",
            type = "Bug",
            author = "kei-1111",
        ),
    ),
)

/** Preview 専用の open Issue 一覧。実リポジトリの Issue のスナップショット。 */
internal val PreviewGitHubIssues = GitHubIssues(
    totalCount = 4,
    issues = persistentListOf(
        GitHubIssue(
            number = 106,
            title = "Add a TODO tool window showing the repository's real open Issues",
            url = "https://github.com/kei-1111/kei-1111.github.io/issues/106",
            type = "Feature",
        ),
        GitHubIssue(
            number = 105,
            title = "Add an interactive Terminal tool window",
            url = "https://github.com/kei-1111/kei-1111.github.io/issues/105",
            type = "Feature",
        ),
        GitHubIssue(
            number = 97,
            title = "Audit the entire codebase for drifted implementations and refactor them",
            url = "https://github.com/kei-1111/kei-1111.github.io/issues/97",
            type = "Refactor",
        ),
        GitHubIssue(
            number = 24,
            title = "作品ページの追加（作品 API + クライアント UI）",
            url = "https://github.com/kei-1111/kei-1111.github.io/issues/24",
            type = "Feature",
        ),
    ),
)

/**
 * feature は :core:data に依存できない（レイヤリングルール）ため、
 * github.com/kei-1111 の実データを Preview 用に複製している。
 */
internal val PreviewProfile = Profile(
    name = LocalizedText(ja = "けい", en = "Kei"),
    handle = "kei-1111",
    location = "Japan",
    role = "Android developer",
    iconUrl = "images/profile-icon.webp",
    followers = 15,
    following = 25,
    repos = 32,
    totalStars = 41,
    pinnedRepos = persistentListOf(
        PinnedRepo(
            name = "kei-1111.github.io",
            description = LocalizedText(ja = "自己紹介Webサイトのリポジトリ", en = "My portfolio website repository"),
            url = "https://github.com/kei-1111/kei-1111.github.io",
            language = RepoLanguage("Kotlin"),
        ),
        PinnedRepo(
            name = "android-template",
            description = LocalizedText(ja = "My Android Template Project", en = "My Android Template Project"),
            url = "https://github.com/kei-1111/android-template",
            language = RepoLanguage("Shell"),
            stars = 2,
        ),
        PinnedRepo(
            name = "kmp-sample-library",
            description = LocalizedText(ja = "KMP Library のサンプルリポジトリ", en = "Sample repository for a KMP library"),
            url = "https://github.com/kei-1111/kmp-sample-library",
            language = RepoLanguage("Kotlin"),
        ),
        PinnedRepo(
            name = "kmp-sample-android",
            description = LocalizedText(
                ja = "KMPライブラリを使うAndroidアプリ",
                en = "Android app using the KMP library",
            ),
            url = "https://github.com/kei-1111/kmp-sample-android",
            language = RepoLanguage("Kotlin"),
        ),
        PinnedRepo(
            name = "kmp-sample-ios",
            description = LocalizedText(ja = "KMPライブラリを使うiOSアプリ", en = "iOS app using the KMP library"),
            url = "https://github.com/kei-1111/kmp-sample-ios",
            language = RepoLanguage("Swift"),
        ),
    ),
    languages = persistentListOf(
        LanguageShare(language = RepoLanguage("Kotlin"), share = 0.87f, color = "#A97BFF"),
        LanguageShare(language = RepoLanguage("Swift"), share = 0.10f, color = "#F05138"),
        LanguageShare(language = RepoLanguage("Shell"), share = 0.02f, color = "#89e051"),
    ),
    links = persistentListOf(
        LinkService(type = LinkServiceType.GitHub, name = "GitHub", url = "https://github.com/kei-1111"),
        LinkService(type = LinkServiceType.X, name = "X", url = "https://x.com/kei_1111_"),
        LinkService(type = LinkServiceType.Qiita, name = "Qiita", url = "https://qiita.com/kei-1111"),
        LinkService(type = LinkServiceType.Note, name = "note", url = "https://note.com/kei_1111_"),
    ),
)

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
 * エントリは実データ（[io.github.kei_1111.app.core.data.license.LicenseContent]）
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
        LicenseEntry(
            name = "Ktor",
            owner = "io.ktor",
            type = LicenseType.Apache20,
            url = "https://github.com/ktorio/ktor",
            copyright = "Copyright JetBrains s.r.o. and contributors.",
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

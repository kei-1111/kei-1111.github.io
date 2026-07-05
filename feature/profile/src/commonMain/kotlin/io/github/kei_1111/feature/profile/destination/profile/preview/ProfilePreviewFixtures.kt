@file:Suppress("MagicNumber")

package io.github.kei_1111.feature.profile.destination.profile.preview

import io.github.kei_1111.core.model.ContributionCalendar
import io.github.kei_1111.core.model.ContributionDay
import io.github.kei_1111.core.model.GitHubProfile
import io.github.kei_1111.core.model.LanguageShare
import io.github.kei_1111.core.model.LinkService
import io.github.kei_1111.core.model.LinkServiceType
import io.github.kei_1111.core.model.PinnedRepo
import io.github.kei_1111.core.model.RepoLanguage
import kotlinx.collections.immutable.persistentListOf
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
        LinkService(type = LinkServiceType.Zenn, name = "Zenn", url = "https://zenn.dev/kei_1111"),
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

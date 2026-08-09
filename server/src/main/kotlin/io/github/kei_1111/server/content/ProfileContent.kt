@file:Suppress("MagicNumber")

package io.github.kei_1111.server.content

import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.persistentListOf

/** 配信するプロフィールのベース値。GitHub API から統計が取れない場合は統計値もこのままフォールバックとして配信される。 */
internal val DefaultGitHubProfile = GitHubProfile(
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
            language = RepoLanguage.Kotlin,
        ),
        PinnedRepo(
            name = "android-template",
            description = LocalizedText(ja = "My Android Template Project", en = "My Android Template Project"),
            url = "https://github.com/kei-1111/android-template",
            stars = 2,
        ),
        PinnedRepo(
            name = "kmp-sample-library",
            description = LocalizedText(ja = "KMP Library のサンプルリポジトリ", en = "Sample repository for a KMP library"),
            url = "https://github.com/kei-1111/kmp-sample-library",
            language = RepoLanguage.Kotlin,
        ),
        PinnedRepo(
            name = "kmp-sample-ios",
            description = LocalizedText(ja = "KMPライブラリを使うiOSアプリ", en = "iOS app using the KMP library"),
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
        LinkService(
            type = LinkServiceType.GitHub,
            name = "GitHub",
            url = "https://github.com/kei-1111",
        ),
        LinkService(
            type = LinkServiceType.X,
            name = "X",
            url = "https://x.com/kei_1111_",
        ),
        LinkService(
            type = LinkServiceType.Qiita,
            name = "Qiita",
            url = "https://qiita.com/kei-1111",
        ),
        LinkService(
            type = LinkServiceType.Note,
            name = "note",
            url = "https://note.com/kei_1111_",
        ),
    ),
)

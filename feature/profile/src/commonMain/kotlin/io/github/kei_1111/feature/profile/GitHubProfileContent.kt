@file:Suppress("MagicNumber")

package io.github.kei_1111.feature.profile

import androidx.compose.ui.graphics.Color
import io.github.kei_1111.core.designsystem.theme.IdeColors
import kei_1111.feature.profile.generated.resources.Res
import kei_1111.feature.profile.generated.resources.ic_link_github
import kei_1111.feature.profile.generated.resources.ic_link_qiita
import kei_1111.feature.profile.generated.resources.ic_link_x
import kei_1111.feature.profile.generated.resources.ic_link_zenn
import org.jetbrains.compose.resources.DrawableResource

/** GitHub プロフィールカードに表示する内容。props として注入できる形にまとめている。 */
data class GitHubProfileData(
    val name: String,
    val handle: String,
    val location: String,
    val role: String,
    val followers: Int,
    val following: Int,
    val repos: Int,
    val totalStars: Int,
    val pinnedRepos: List<PinnedRepo>,
    val languages: List<LanguageShare>,
    val links: List<LinkService>,
)

/** Pinned セクションの1行。右端は言語ドット（language）か ★ 数（stars）のどちらか。 */
data class PinnedRepo(
    val name: String,
    val description: String,
    val url: String,
    val language: RepoLanguage? = null,
    val stars: Int? = null,
)

enum class RepoLanguage(val displayName: String) {
    Kotlin("Kotlin"),
    Swift("Swift"),
    Shell("Shell"),
}

/** LANGUAGES バーの1言語分。share は 0..1 の割合。 */
data class LanguageShare(
    val language: RepoLanguage,
    val share: Float,
)

/** LINKS グリッドの1タイル。brandColor はアイコン tint とホバー枠線の両方に使う。 */
data class LinkService(
    val name: String,
    val url: String,
    val icon: DrawableResource,
    val brandColor: Color,
)

/** github.com/kei-1111 のプロフィールを凝縮したデフォルト値。 */
data object GitHubProfileContent {
    val default = GitHubProfileData(
        name = "けい",
        handle = "kei-1111",
        location = "Japan",
        role = "Android developer",
        followers = 15,
        following = 25,
        repos = 32,
        totalStars = 41,
        pinnedRepos = listOf(
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
        languages = listOf(
            LanguageShare(language = RepoLanguage.Kotlin, share = 0.78f),
            LanguageShare(language = RepoLanguage.Swift, share = 0.12f),
            LanguageShare(language = RepoLanguage.Shell, share = 0.10f),
        ),
        links = listOf(
            LinkService(
                name = "GitHub",
                url = "https://github.com/kei-1111",
                icon = Res.drawable.ic_link_github,
                brandColor = IdeColors.TextPrimary,
            ),
            LinkService(
                name = "X",
                url = "https://x.com/kei_1111_",
                icon = Res.drawable.ic_link_x,
                brandColor = IdeColors.TextPrimary,
            ),
            LinkService(
                name = "Qiita",
                url = "https://qiita.com/kei-1111",
                icon = Res.drawable.ic_link_qiita,
                brandColor = IdeColors.BrandQiita,
            ),
            LinkService(
                name = "Zenn",
                url = "https://zenn.dev/kei_1111",
                icon = Res.drawable.ic_link_zenn,
                brandColor = IdeColors.BrandZenn,
            ),
        ),
    )
}

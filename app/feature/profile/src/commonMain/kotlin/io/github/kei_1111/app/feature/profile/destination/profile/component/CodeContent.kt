package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.core.designsystem.theme.KeiColorScheme
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.highlightMarkdown
import io.github.kei_1111.app.feature.profile.destination.profile.model.profileCode
import io.github.kei_1111.app.feature.profile.destination.profile.model.worksCode
import io.github.kei_1111.app.feature.profile.destination.profile.theme.highlightKotlin
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import io.github.kei_1111.shared.model.Work
import kotlinx.collections.immutable.ImmutableList

/**
 * [profile] / [works] は各ページの分岐でのみ使う。null（取得待ち）はその分岐に到達しない前提
 * （呼び出し側が [EditorCodeArea] でスケルトン表示に切り替える）だが、型としては null 安全に扱う。
 */
internal fun codeLinesFor(
    page: EditorPage,
    profile: GitHubProfile?,
    licenses: ThirdPartyLicenses?,
    works: ImmutableList<Work>?,
    language: KeiLanguage,
    japaneseFontFamily: FontFamily,
    colors: KeiColorScheme,
): List<AnnotatedString> = when (page) {
    EditorPage.Readme -> highlightMarkdown(readmeBlocks(language), japaneseFontFamily, colors)
    EditorPage.Profile -> profile?.let { highlightKotlin(profileCode(it, language), japaneseFontFamily, colors) }.orEmpty()
    EditorPage.Works -> works?.let { highlightKotlin(worksCode(it, language), japaneseFontFamily, colors) }.orEmpty()
    EditorPage.Licenses -> highlightKotlin(licenseCode(licenses), japaneseFontFamily, colors)
}

internal fun usageCodeLines(
    language: KeiLanguage,
    japaneseFontFamily: FontFamily,
    colors: KeiColorScheme,
): List<AnnotatedString> = highlightKotlin(usageCode(language), japaneseFontFamily, colors)

private fun usageCode(language: KeiLanguage): String = when (language) {
    KeiLanguage.Ja -> """
    |// このサイトの使い方
    |//
    |// すべてのタブを閉じました。左のプロジェクトツリーから
    |// ファイルを開くと、エディタとプレビューが再び表示されます。
    |//
    |// - 左端レールの Project アイコン : プロジェクトツリーを開閉
    |// - ツリーの README.md / ProfileScreen.kt / WorksScreen.kt / LicenseScreen.kt : ページを開く
    |// - タブ右端のボタン : Code / Split / Design の表示切替
    |// - Preview 右下のコントロール : ズーム（+ / − / 1:1 / fit）
    |// - Preview 内のカード : リンクやライセンスは実際に操作できます
    |// - タイトルバー右端のボタン : 言語とダーク / ライトテーマの切替
    """.trimMargin()

    KeiLanguage.En -> """
    |// How to use this site
    |//
    |// All tabs are closed. Open a file from the project tree
    |// on the left to bring the editor and preview back.
    |//
    |// - Project icon on the left rail : open / close the project tree
    |// - README.md / ProfileScreen.kt / WorksScreen.kt / LicenseScreen.kt in the tree : open a page
    |// - Buttons at the right end of the tab bar : switch Code / Split / Design
    |// - Controls at the bottom right of Preview : zoom (+ / − / 1:1 / fit)
    |// - Cards inside Preview : links and licenses actually work
    |// - Buttons at the right end of the title bar : switch language and theme
    """.trimMargin()
}

private fun licenseEntryCode(entry: LicenseEntry): String = listOf(
    "|            LicenseEntry(",
    "|                name = \"${entry.name}\",",
    "|                owner = \"${entry.owner}\",",
    "|                license = \"${entry.type.id}\",",
    "|                url = \"${entry.url}\",",
    "|            ),",
).joinToString("\n")

private fun licenseCode(licenses: ThirdPartyLicenses?): String {
    val entries = licenses?.let { it.icons + it.fonts + it.app + it.server }.orEmpty()
    return """
    |package io.github.kei_1111.ui.license
    |
    |import ...
    |
    |@Composable
    |internal fun LicenseScreen(
    |    licenses: List<LicenseEntry>,
    |    modifier: Modifier = Modifier,
    |) { ... }
    |
    |@Preview
    |@Composable
    |private fun LicenseScreenPreview() {
    |    LicenseScreen(
    |        licenses = listOf(
    ${entries.joinToString("\n") { licenseEntryCode(it) }}
    |        ),
    |    )
    |}
    """.trimMargin()
}

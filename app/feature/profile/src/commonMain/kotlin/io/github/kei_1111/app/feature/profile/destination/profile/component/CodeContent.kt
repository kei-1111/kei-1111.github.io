package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import io.github.kei_1111.app.core.designsystem.theme.KeiColorScheme
import io.github.kei_1111.app.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.highlightMarkdown
import io.github.kei_1111.app.feature.profile.destination.profile.profileCode
import io.github.kei_1111.app.feature.profile.theme.highlightKotlin
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.ThirdPartyLicenses

/** 各ページに対応するコード（行ごとの AnnotatedString）を返す。 */
internal fun codeLinesFor(
    page: EditorPage,
    profile: GitHubProfile,
    licenses: ThirdPartyLicenses?,
    japaneseFontFamily: FontFamily,
    colors: KeiColorScheme,
): List<AnnotatedString> = when (page) {
    EditorPage.Readme -> highlightMarkdown(ReadmeBlocks, japaneseFontFamily, colors)
    EditorPage.Profile -> highlightKotlin(profileCode(profile), japaneseFontFamily, colors)
    EditorPage.Licenses -> highlightKotlin(licenseCode(licenses), japaneseFontFamily, colors)
}

private fun licenseEntryCode(entry: LicenseEntry): String = listOf(
    "|            LicenseEntry(",
    "|                name = \"${entry.name}\", owner = \"${entry.owner}\",",
    "|                license = \"${entry.type.id}\", url = \"${entry.url}\",",
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

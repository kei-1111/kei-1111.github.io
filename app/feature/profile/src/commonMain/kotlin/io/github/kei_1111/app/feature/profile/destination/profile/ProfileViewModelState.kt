package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.Dp
import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.mvi.ViewModelState
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownBlock
import io.github.kei_1111.app.feature.profile.destination.profile.component.readmeBlocks
import io.github.kei_1111.app.feature.profile.destination.profile.component.readmeSource
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.model.profileCode
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ProfileViewModelState(
    /** 表示言語。KeiLanguageController の値を observeLanguage が同期する（生成コードの言語決定用）。 */
    val language: KeiLanguage = KeiLanguage.Ja,
    val selectedPage: EditorPage? = EditorPage.Readme,
    val openPages: ImmutableList<EditorPage> = persistentListOf(EditorPage.Readme),
    val desktopTreeOpen: Boolean = true,
    val desktopViewMode: EditorViewMode = EditorViewMode.Split,
    val mobileTreeOpen: Boolean = false,
    val mobileViewMode: EditorViewMode = EditorViewMode.PreviewOnly,
    /** ツリーと違いレイアウト非依存。ブレークポイントを跨いでも開閉状態を維持する。 */
    val logcatOpen: Boolean = false,
    /** Logcat パネルの高さ。開閉状態と同様レイアウト非依存で、ドラッグリサイズの結果を保持する。 */
    val logcatPanelHeight: Dp = ProfileDimensions.LogcatPanelHeight,
    val logEntries: ImmutableList<LogEntry> = persistentListOf(),
    val currentLayout: WindowLayout? = null,
    val profileResult: Result<GitHubProfile> = Result.Loading,
    val contributionsResult: Result<ContributionCalendar> = Result.Loading,
    val licensesResult: Result<ThirdPartyLicenses> = Result.Loading,
    /** null = 未編集（生成コードを表示）。 */
    val editedProfileCode: String? = null,
    /** 最後にパース成功した編集結果。 */
    val parsedProfile: GitHubProfile? = null,
    val profileCodeError: Boolean = false,
    /** null = 未編集（生成 Markdown を表示）。 */
    val editedReadmeCode: String? = null,
    /** 最後にパースした README 編集結果。 */
    val parsedReadmeBlocks: ImmutableList<MarkdownBlock>? = null,
    /** 増加するとそのページのエディタ TextFieldState を作り直す。バッファ毎に分け、編集中の側を巻き込まない。 */
    val profileEditorResetTick: Int = 0,
    val readmeEditorResetTick: Int = 0,
    val selectedLicense: LicenseEntry? = null,
    val effect: ProfileEffect? = null,
) : ViewModelState<ProfileState> {
    override fun toState(): ProfileState {
        val loadedProfile = (profileResult as? Result.Success<GitHubProfile>)?.data
        return ProfileState(
            selectedPage = selectedPage,
            openPages = openPages,
            desktopTreeOpen = desktopTreeOpen,
            desktopViewMode = desktopViewMode,
            mobileTreeOpen = mobileTreeOpen,
            mobileViewMode = mobileViewMode,
            logcatOpen = logcatOpen,
            logcatPanelHeight = logcatPanelHeight,
            logEntries = logEntries,
            profile = parsedProfile ?: loadedProfile,
            contributions = (contributionsResult as? Result.Success<ContributionCalendar>)?.data,
            licenses = (licensesResult as? Result.Success<ThirdPartyLicenses>)?.data,
            profileEditorCode = editedProfileCode ?: loadedProfile?.let { profileCode(it, language) }.orEmpty(),
            readmeEditorCode = editedReadmeCode ?: readmeSource(language),
            readmeBlocks = parsedReadmeBlocks ?: readmeBlocks(language),
            profileCodeError = profileCodeError,
            profileEditorResetTick = profileEditorResetTick,
            readmeEditorResetTick = readmeEditorResetTick,
            selectedLicense = selectedLicense,
            effect = effect,
        )
    }
}

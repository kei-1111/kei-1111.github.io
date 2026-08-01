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
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLine
import io.github.kei_1111.app.feature.profile.destination.profile.model.profileCode
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ProfileViewModelState(
    /** KeiLanguageController の値を observeLanguage が同期する（生成コードの言語決定用）。 */
    val language: KeiLanguage = KeiLanguage.Ja,
    val selectedPage: EditorPage? = EditorPage.Readme,
    val openPages: ImmutableList<EditorPage> = persistentListOf(EditorPage.Readme),
    val desktopTreeOpen: Boolean = true,
    val desktopViewMode: EditorViewMode = EditorViewMode.Split,
    val mobileTreeOpen: Boolean = false,
    val mobileViewMode: EditorViewMode = EditorViewMode.PreviewOnly,
    /** ツリーと違いレイアウト非依存。ブレークポイントを跨いでも開閉状態を維持する。 */
    val logcatOpen: Boolean = false,
    /** 実 AS の下部ドックと同様 Logcat と排他で開く。 */
    val todoOpen: Boolean = false,
    /** 開閉状態と同様レイアウト非依存で、ドラッグリサイズの結果を保持する。 */
    val logcatPanelHeight: Dp = ProfileDimensions.LogcatPanelHeight,
    /** Logcat と同様レイアウト非依存で、ドラッグリサイズの結果を保持する。 */
    val todoPanelHeight: Dp = ProfileDimensions.TodoPanelHeight,
    val logEntries: ImmutableList<LogEntry> = persistentListOf(),
    /** Logcat / TODO と同じくレイアウト非依存。下部スロットは1つなので互いに排他で開く。 */
    val terminalOpen: Boolean = false,
    /** Enter で実行されると空に戻る。 */
    val terminalInput: String = "",
    /** エコー行 + 出力行、古い順。 */
    val terminalLines: ImmutableList<TerminalLine> = persistentListOf(),
    /** Logcat と同様レイアウト非依存で、ドラッグリサイズの結果を保持する。 */
    val terminalPanelHeight: Dp = ProfileDimensions.TerminalPanelHeight,
    /** App が所有する状態を UpdateTheme で同期した写し（theme コマンドの判定用）。null = 未同期。 */
    val isDarkTheme: Boolean? = null,
    /** `./gradlew build` リプレイの実行中フラグ（多重起動ガード）。 */
    val terminalBuildRunning: Boolean = false,
    val currentLayout: WindowLayout? = null,
    val profileResult: Result<GitHubProfile> = Result.Loading,
    val contributionsResult: Result<ContributionCalendar> = Result.Loading,
    val issuesResult: Result<GitHubIssues> = Result.Loading,
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
            todoOpen = todoOpen,
            logcatPanelHeight = logcatPanelHeight,
            todoPanelHeight = todoPanelHeight,
            logEntries = logEntries,
            terminalOpen = terminalOpen,
            terminalInput = terminalInput,
            terminalLines = terminalLines,
            terminalPanelHeight = terminalPanelHeight,
            profile = parsedProfile ?: loadedProfile,
            contributions = (contributionsResult as? Result.Success<ContributionCalendar>)?.data,
            issues = (issuesResult as? Result.Success<GitHubIssues>)?.data,
            profileLoadFailed = profileResult is Result.Error,
            contributionsLoadFailed = contributionsResult is Result.Error,
            issuesLoadFailed = issuesResult is Result.Error,
            licenses = (licensesResult as? Result.Success<ThirdPartyLicenses>)?.data,
            profileEditorCode = editedProfileCode ?: loadedProfile?.let { profileCode(it, language) }.orEmpty(),
            readmeEditorCode = editedReadmeCode ?: readmeSource(language),
            readmeBlocks = parsedReadmeBlocks ?: readmeBlocks(language),
            profileCodeError = profileCodeError,
            languageToggleEnabled = editedProfileCode == null && editedReadmeCode == null,
            profileEditorResetTick = profileEditorResetTick,
            readmeEditorResetTick = readmeEditorResetTick,
            selectedLicense = selectedLicense,
            effect = effect,
        )
    }
}

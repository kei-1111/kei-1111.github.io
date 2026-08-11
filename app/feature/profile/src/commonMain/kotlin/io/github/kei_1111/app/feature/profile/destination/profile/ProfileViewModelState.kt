package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.Dp
import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.common.result.successOrNull
import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.mvi.ViewModelState
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.markdownSource
import io.github.kei_1111.app.feature.profile.destination.profile.model.BottomTool
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.model.ProfileBalloon
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLine
import io.github.kei_1111.app.feature.profile.destination.profile.model.blocksFor
import io.github.kei_1111.app.feature.profile.destination.profile.model.profileCode
import io.github.kei_1111.app.feature.profile.destination.profile.model.worksCode
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.Profile
import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.TerminalTextCommands
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.Works
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
    /** 開いている下部ツールウィンドウ（null = すべて閉）。ツリーと違いレイアウト非依存で、ブレークポイントを跨いでも開閉状態を維持する。 */
    val openBottomTool: BottomTool? = null,
    /** 開閉状態と同様レイアウト非依存で、ドラッグリサイズの結果を保持する。 */
    val logcatPanelHeight: Dp = ProfileDimensions.LogcatPanelHeight,
    /** Logcat と同様レイアウト非依存で、ドラッグリサイズの結果を保持する。 */
    val todoPanelHeight: Dp = ProfileDimensions.TodoPanelHeight,
    /** Logcat と同様レイアウト非依存で、ドラッグリサイズの結果を保持する。 */
    val changelogPanelHeight: Dp = ProfileDimensions.ChangelogPanelHeight,
    val logEntries: ImmutableList<LogEntry> = persistentListOf(),
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
    val profileResult: Result<Profile> = Result.Loading,
    val contributionsResult: Result<ContributionCalendar> = Result.Loading,
    val issuesResult: Result<GitHubIssues> = Result.Loading,
    val worksResult: Result<Works> = Result.Loading,
    val readmeResult: Result<Readme> = Result.Loading,
    val terminalCommandsResult: Result<TerminalTextCommands> = Result.Loading,
    val changelogResult: Result<GitHubChangelog> = Result.Loading,
    val licensesResult: Result<ThirdPartyLicenses> = Result.Loading,
    /** null = 未編集（生成コードを表示）。 */
    val editedProfileCode: String? = null,
    /** 最後にパース成功した編集結果。 */
    val parsedProfile: Profile? = null,
    val profileCodeError: Boolean = false,
    /** null = 未編集（生成 Markdown を表示）。 */
    val editedReadmeCode: String? = null,
    /** 最後にパースした README 編集結果。 */
    val parsedReadmeBlocks: ImmutableList<MarkdownBlock>? = null,
    /** null = 未編集（生成コードを表示）。 */
    val editedWorksCode: String? = null,
    /** 最後にパース成功した works 編集結果（アセット復元済み）。 */
    val parsedWorks: ImmutableList<Work>? = null,
    val worksCodeError: Boolean = false,
    /** 増加するとそのページのエディタ TextFieldState を作り直す。バッファ毎に分け、編集中の側を巻き込まない。 */
    val profileEditorResetTick: Int = 0,
    val readmeEditorResetTick: Int = 0,
    val worksEditorResetTick: Int = 0,
    val selectedLicense: LicenseEntry? = null,
    val worksSheetOpen: Boolean = false,
    val balloons: ImmutableList<ProfileBalloon> = persistentListOf(),
    val effect: ProfileEffect? = null,
) : ViewModelState<ProfileState> {
    override fun toState(): ProfileState {
        val loadedProfile = profileResult.successOrNull
        return ProfileState(
            selectedPage = selectedPage,
            openPages = openPages,
            desktopTreeOpen = desktopTreeOpen,
            desktopViewMode = desktopViewMode,
            mobileTreeOpen = mobileTreeOpen,
            mobileViewMode = mobileViewMode,
            openBottomTool = openBottomTool,
            logcatPanelHeight = logcatPanelHeight,
            todoPanelHeight = todoPanelHeight,
            changelogPanelHeight = changelogPanelHeight,
            logEntries = logEntries,
            terminalInput = terminalInput,
            terminalLines = terminalLines,
            terminalPanelHeight = terminalPanelHeight,
            profile = parsedProfile ?: loadedProfile,
            contributions = contributionsResult.successOrNull,
            issues = issuesResult.successOrNull,
            changelog = changelogResult.successOrNull,
            works = parsedWorks ?: worksResult.successOrNull?.items,
            profileLoadFailed = profileResult is Result.Error,
            contributionsLoadFailed = contributionsResult is Result.Error,
            issuesLoadFailed = issuesResult is Result.Error,
            changelogLoadFailed = changelogResult is Result.Error,
            worksLoadFailed = worksResult is Result.Error,
            readmeLoadFailed = readmeResult is Result.Error,
            licenses = licensesResult.successOrNull,
            profileEditorCode = editedProfileCode ?: loadedProfile?.let { profileCode(it, language) }.orEmpty(),
            readmeEditorCode = editedReadmeCode
                ?: readmeResult.successOrNull?.let { markdownSource(it.blocksFor(language)) }.orEmpty(),
            worksEditorCode = editedWorksCode
                ?: worksResult.successOrNull?.items?.let { worksCode(it, language) }.orEmpty(),
            readmeBlocks = parsedReadmeBlocks ?: readmeResult.successOrNull?.blocksFor(language),
            profileCodeError = profileCodeError,
            worksCodeError = worksCodeError,
            languageToggleEnabled = editedProfileCode == null && editedReadmeCode == null && editedWorksCode == null,
            profileEditorResetTick = profileEditorResetTick,
            readmeEditorResetTick = readmeEditorResetTick,
            worksEditorResetTick = worksEditorResetTick,
            selectedLicense = selectedLicense,
            worksSheetOpen = worksSheetOpen,
            balloons = balloons,
            effect = effect,
        )
    }
}

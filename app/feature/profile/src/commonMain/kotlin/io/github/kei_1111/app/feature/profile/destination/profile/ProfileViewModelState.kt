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
import io.github.kei_1111.app.feature.profile.destination.profile.model.LoadPhase
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLine
import io.github.kei_1111.app.feature.profile.destination.profile.model.blocksFor
import io.github.kei_1111.app.feature.profile.destination.profile.model.profileCode
import io.github.kei_1111.app.feature.profile.destination.profile.model.worksCode
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.app.feature.profile.model.isReadOnly
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.MarkdownBlock
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
    val profileResult: Result<GitHubProfile> = Result.Loading,
    val contributionsResult: Result<ContributionCalendar> = Result.Loading,
    val issuesResult: Result<GitHubIssues> = Result.Loading,
    val worksResult: Result<Works> = Result.Loading,
    val readmeResult: Result<Readme> = Result.Loading,
    val terminalCommandsResult: Result<TerminalTextCommands> = Result.Loading,
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
    /** タブ・レイアウト切替を跨いで維持するため、カルーセル位置もレイアウト非依存で保持する。 */
    val selectedWorkIndex: Int = 0,
    val worksScreenshotIndex: Int = 0,
    val effect: ProfileEffect? = null,
) : ViewModelState<ProfileState> {
    /**
     * 表示フェーズは「データが来ていれば Ready、来ておらず取得に失敗しているなら Failed、それ以外は Loading」。
     * README は null（未取得）と空リスト（編集で全消し）を区別し、空でも Ready のまま編集を続けさせる。
     */
    private fun previewPhaseFor(page: EditorPage?, worksItems: ImmutableList<Work>?): LoadPhase = when (page) {
        // ライセンスは flowOf の静的コンテンツで取得待ちも失敗もなく、再試行導線も持たない
        null, EditorPage.Licenses -> LoadPhase.Ready
        EditorPage.Profile -> loadPhase(
            ready = (parsedProfile ?: profileResult.successOrNull) != null,
            failed = profileResult is Result.Error,
        )

        EditorPage.Works -> loadPhase(
            ready = !worksItems.isNullOrEmpty(),
            failed = worksResult is Result.Error,
        )

        EditorPage.Readme -> loadPhase(
            ready = (parsedReadmeBlocks ?: readmeResult.successOrNull?.blocksFor(language)) != null,
            failed = readmeResult is Result.Error,
        )
    }

    private fun loadPhase(ready: Boolean, failed: Boolean): LoadPhase = when {
        ready -> LoadPhase.Ready
        failed -> LoadPhase.Failed
        else -> LoadPhase.Loading
    }

    override fun toState(): ProfileState {
        val loadedProfile = profileResult.successOrNull
        val worksItems = parsedWorks ?: worksResult.successOrNull?.items
        return ProfileState(
            selectedPage = selectedPage,
            openPages = openPages,
            isDesktopTreeOpen = desktopTreeOpen,
            desktopViewMode = desktopViewMode,
            isMobileTreeOpen = mobileTreeOpen,
            mobileViewMode = mobileViewMode,
            openBottomTool = openBottomTool,
            isLogcatOpen = openBottomTool == BottomTool.Logcat,
            isTodoOpen = openBottomTool == BottomTool.Todo,
            isTerminalOpen = openBottomTool == BottomTool.Terminal,
            isSelectedPageReadOnly = selectedPage?.isReadOnly == true,
            logcatPanelHeight = logcatPanelHeight,
            todoPanelHeight = todoPanelHeight,
            logEntries = logEntries,
            terminalInput = terminalInput,
            terminalLines = terminalLines,
            terminalPanelHeight = terminalPanelHeight,
            profile = parsedProfile ?: loadedProfile,
            contributions = contributionsResult.successOrNull,
            issues = issuesResult.successOrNull,
            works = worksItems,
            previewPhase = previewPhaseFor(selectedPage, worksItems),
            contributionsPhase = loadPhase(
                ready = contributionsResult.successOrNull != null,
                failed = contributionsResult is Result.Error,
            ),
            issuesPhase = loadPhase(
                ready = issuesResult.successOrNull != null,
                failed = issuesResult is Result.Error,
            ),
            licenses = licensesResult.successOrNull,
            profileEditorCode = editedProfileCode ?: loadedProfile?.let { profileCode(it, language) }.orEmpty(),
            readmeEditorCode = editedReadmeCode
                ?: readmeResult.successOrNull?.let { markdownSource(it.blocksFor(language)) }.orEmpty(),
            worksEditorCode = editedWorksCode
                ?: worksResult.successOrNull?.items?.let { worksCode(it, language) }.orEmpty(),
            readmeBlocks = parsedReadmeBlocks ?: readmeResult.successOrNull?.blocksFor(language),
            hasProfileCodeError = profileCodeError,
            hasWorksCodeError = worksCodeError,
            isLanguageToggleEnabled = editedProfileCode == null && editedReadmeCode == null && editedWorksCode == null,
            profileEditorResetTick = profileEditorResetTick,
            readmeEditorResetTick = readmeEditorResetTick,
            worksEditorResetTick = worksEditorResetTick,
            selectedLicense = selectedLicense,
            isWorksSheetOpen = worksSheetOpen,
            // リスト差し替えで縮んでも範囲外参照にならないよう、公開時に現在のリスト範囲へ丸める
            selectedWorkIndex = selectedWorkIndex.coerceIn(0, (worksItems?.lastIndex ?: 0).coerceAtLeast(0)),
            worksScreenshotIndex = worksScreenshotIndex,
            effect = effect,
        )
    }
}

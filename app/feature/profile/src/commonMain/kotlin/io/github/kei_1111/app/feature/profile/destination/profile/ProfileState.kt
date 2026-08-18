package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.Dp
import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.mvi.State
import io.github.kei_1111.app.feature.profile.destination.profile.model.BottomTool
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.model.LoadPhase
import io.github.kei_1111.app.feature.profile.destination.profile.model.ProfileBalloon
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLine
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.Profile
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import io.github.kei_1111.shared.model.Work
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ProfileState(
    /** openPages が空のときに限り null（全タブを閉じた状態）。 */
    val selectedPage: EditorPage? = EditorPage.Readme,
    /** 開いた順。ProjectTree から開くと追加される。 */
    val openPages: ImmutableList<EditorPage> = persistentListOf(EditorPage.Readme),
    val isDesktopTreeOpen: Boolean = true,
    val desktopViewMode: EditorViewMode = EditorViewMode.Split,
    val isMobileTreeOpen: Boolean = false,
    val mobileViewMode: EditorViewMode = EditorViewMode.PreviewOnly,
    /** 開いている下部ツールウィンドウ（null = すべて閉）。ツリーと違いレイアウト非依存で、ブレークポイントを跨いでも開閉状態を維持する。 */
    val openBottomTool: BottomTool? = null,
    /** ツールレールの3トグルの点灯状態。Desktop / Mobile が同じ比較を持たないよう State で決める。 */
    val isLogcatOpen: Boolean = false,
    val isTodoOpen: Boolean = false,
    val isTerminalOpen: Boolean = false,
    val isChangelogOpen: Boolean = false,
    /** 選択ページが編集不可か（生成コードを読み取り専用で見せる）。 */
    val isSelectedPageReadOnly: Boolean = false,
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
    val profile: Profile? = null,
    val contributions: ContributionCalendar? = null,
    /** TODO ツールウィンドウに表示する。 */
    val issues: GitHubIssues? = null,
    /** Git ツールウィンドウに表示するマージ済み Pull Request 一覧。 */
    val changelog: GitHubChangelog? = null,
    val works: ImmutableList<Work>? = null,
    /**
     * 選択ページの表示フェーズ。エディタのスケルトンと Preview のビルド表示が共有する。
     * 全タブを閉じている間は島自体を描かないため参照されない。
     */
    val previewPhase: LoadPhase = LoadPhase.Loading,
    /** Contributions / TODO は選択ページと独立に落ちうるため、セクションごとにフェーズを持つ。 */
    val contributionsPhase: LoadPhase = LoadPhase.Loading,
    val issuesPhase: LoadPhase = LoadPhase.Loading,
    val changelogPhase: LoadPhase = LoadPhase.Loading,
    val licenses: ThirdPartyLicenses? = null,
    val profileEditorCode: String = "",
    val readmeEditorCode: String = "",
    val readmeBlocks: ImmutableList<MarkdownBlock>? = null,
    val hasProfileCodeError: Boolean = false,
    val worksEditorCode: String = "",
    val hasWorksCodeError: Boolean = false,
    /** 編集済みバッファは言語切替に追従しないため、編集がある間は言語トグルを無効化する。 */
    val isLanguageToggleEnabled: Boolean = true,
    val profileEditorResetTick: Int = 0,
    val readmeEditorResetTick: Int = 0,
    val worksEditorResetTick: Int = 0,
    val selectedLicense: LicenseEntry? = null,
    val isWorksSheetOpen: Boolean = false,
    val selectedWorkIndex: Int = 0,
    val worksScreenshotIndex: Int = 0,
    /** 右下に積むバルーン通知。先頭が上、末尾が最新。 */
    val balloons: ImmutableList<ProfileBalloon> = persistentListOf(),
    val effect: ProfileEffect? = null,
) : State {
    /** ペインの表示可否はレイアウトごとのビューモードで決まる。呼び出し側は自分のレイアウトを渡す。 */
    fun isEditorPaneVisible(layout: WindowLayout): Boolean = viewModeFor(layout) != EditorViewMode.PreviewOnly

    fun isPreviewPaneVisible(layout: WindowLayout): Boolean = viewModeFor(layout) != EditorViewMode.CodeOnly

    fun isSplit(layout: WindowLayout): Boolean = viewModeFor(layout) == EditorViewMode.Split

    private fun viewModeFor(layout: WindowLayout): EditorViewMode =
        if (layout == WindowLayout.Mobile) mobileViewMode else desktopViewMode

    fun editorResetTickFor(page: EditorPage): Int = when (page) {
        EditorPage.Readme -> readmeEditorResetTick
        EditorPage.Works -> worksEditorResetTick
        else -> profileEditorResetTick
    }

    fun editorCodeFor(page: EditorPage): String = when (page) {
        EditorPage.Readme -> readmeEditorCode
        EditorPage.Works -> worksEditorCode
        else -> profileEditorCode
    }

    fun hasCodeError(page: EditorPage): Boolean = when (page) {
        EditorPage.Profile -> hasProfileCodeError
        EditorPage.Works -> hasWorksCodeError
        else -> false
    }
}

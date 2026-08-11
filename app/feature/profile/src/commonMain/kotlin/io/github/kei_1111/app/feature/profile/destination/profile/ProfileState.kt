package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.Dp
import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.mvi.State
import io.github.kei_1111.app.feature.profile.destination.profile.model.BottomTool
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.model.ProfileBalloon
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLine
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import io.github.kei_1111.shared.model.Work
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ProfileState(
    /** openPages が空のときに限り null（全タブを閉じた状態）。 */
    val selectedPage: EditorPage? = EditorPage.Readme,
    /** 開いた順。ProjectTree から開くと追加される。 */
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
    val profile: GitHubProfile? = null,
    val contributions: ContributionCalendar? = null,
    /** TODO ツールウィンドウに表示する。 */
    val issues: GitHubIssues? = null,
    /** Git ツールウィンドウに表示するマージ済み Pull Request 一覧。 */
    val changelog: GitHubChangelog? = null,
    val works: ImmutableList<Work>? = null,
    /** Preview のエラー行＋再試行リンク表示に使う。 */
    val profileLoadFailed: Boolean = false,
    val contributionsLoadFailed: Boolean = false,
    val issuesLoadFailed: Boolean = false,
    val changelogLoadFailed: Boolean = false,
    val worksLoadFailed: Boolean = false,
    val licenses: ThirdPartyLicenses? = null,
    val profileEditorCode: String = "",
    val readmeEditorCode: String = "",
    val readmeBlocks: ImmutableList<MarkdownBlock>? = null,
    val readmeLoadFailed: Boolean = false,
    val profileCodeError: Boolean = false,
    val worksEditorCode: String = "",
    val worksCodeError: Boolean = false,
    /** 編集済みバッファは言語切替に追従しないため、編集がある間は言語トグルを無効化する。 */
    val languageToggleEnabled: Boolean = true,
    val profileEditorResetTick: Int = 0,
    val readmeEditorResetTick: Int = 0,
    val worksEditorResetTick: Int = 0,
    val selectedLicense: LicenseEntry? = null,
    val worksSheetOpen: Boolean = false,
    /** 右下に積むバルーン通知。先頭が上、末尾が最新。 */
    val balloons: ImmutableList<ProfileBalloon> = persistentListOf(),
    val effect: ProfileEffect? = null,
) : State {
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

    fun codeErrorFor(page: EditorPage): Boolean = when (page) {
        EditorPage.Profile -> profileCodeError
        EditorPage.Works -> worksCodeError
        else -> false
    }
}

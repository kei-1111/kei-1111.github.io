package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.Dp
import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.core.mvi.State
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownBlock
import io.github.kei_1111.app.feature.profile.destination.profile.component.readmeBlocks
import io.github.kei_1111.app.feature.profile.destination.profile.component.readmeSource
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLine
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.ThirdPartyLicenses
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
    val profile: GitHubProfile? = null,
    val contributions: ContributionCalendar? = null,
    /** TODO ツールウィンドウに表示する。 */
    val issues: GitHubIssues? = null,
    /** Preview のエラー行＋再試行リンク表示に使う。 */
    val profileLoadFailed: Boolean = false,
    val contributionsLoadFailed: Boolean = false,
    val issuesLoadFailed: Boolean = false,
    val licenses: ThirdPartyLicenses? = null,
    val profileEditorCode: String = "",
    /**
     * 実行時の初期値は ProfileViewModel.createInitialState() が初期 ViewModelState から導出する
     * （言語込みで一致させるため）。この既定値は Preview / fixtures 用。
     */
    val readmeEditorCode: String = readmeSource(KeiLanguage.Ja),
    val readmeBlocks: ImmutableList<MarkdownBlock> = readmeBlocks(KeiLanguage.Ja),
    val profileCodeError: Boolean = false,
    /** 編集済みバッファは言語切替に追従しないため、編集がある間は言語トグルを無効化する。 */
    val languageToggleEnabled: Boolean = true,
    val profileEditorResetTick: Int = 0,
    val readmeEditorResetTick: Int = 0,
    val selectedLicense: LicenseEntry? = null,
    val effect: ProfileEffect? = null,
) : State {
    fun editorResetTickFor(page: EditorPage): Int =
        if (page == EditorPage.Readme) readmeEditorResetTick else profileEditorResetTick
}

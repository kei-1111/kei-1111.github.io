package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.Dp
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.mvi.Intent
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.LicenseEntry

internal sealed interface ProfileIntent : Intent {
    data class UpdateLayout(val layout: WindowLayout) : ProfileIntent
    data class UpdateSelectedPage(val page: EditorPage) : ProfileIntent
    data class UpdateSelectedPageFromTree(val page: EditorPage, val layout: WindowLayout) : ProfileIntent
    data class ClosePage(val page: EditorPage) : ProfileIntent
    data class ToggleTree(val layout: WindowLayout) : ProfileIntent
    data object ToggleLogcat : ProfileIntent
    data object ToggleTodo : ProfileIntent
    data object ToggleTerminal : ProfileIntent
    data object ToggleChangelog : ProfileIntent
    data class UpdateTerminalInput(val value: String) : ProfileIntent
    data object ExecuteTerminalCommand : ProfileIntent
    data class UpdateTerminalPanelHeight(val height: Dp) : ProfileIntent

    /** 現在のテーマを環境から同期する（`UpdateLayout` と同じ環境プッシュ。theme コマンドの判定に使う）。 */
    data class UpdateTheme(val isDark: Boolean) : ProfileIntent
    data object ClearLogcat : ProfileIntent
    data class UpdateLogcatPanelHeight(val height: Dp) : ProfileIntent
    data class UpdateTodoPanelHeight(val height: Dp) : ProfileIntent
    data class UpdateChangelogPanelHeight(val height: Dp) : ProfileIntent
    data class UpdateViewMode(val viewMode: EditorViewMode, val layout: WindowLayout) : ProfileIntent
    data class UpdateProfileCode(val code: String) : ProfileIntent
    data class UpdateReadmeCode(val code: String) : ProfileIntent
    data class UpdateWorksCode(val code: String) : ProfileIntent
    data object ResetEditorCode : ProfileIntent
    data class OpenPage(val page: EditorPage) : ProfileIntent
    data object OpenSearchEverywhere : ProfileIntent
    data class OpenUrl(val url: String) : ProfileIntent

    /** null = シート閉じる。 */
    data class UpdateSelectedLicense(val license: LicenseEntry?) : ProfileIntent

    data class UpdateWorksSheetVisibility(val isVisible: Boolean) : ProfileIntent

    /** バックエンドデータ（profile / contributions / issues / works）のうち、取得に失敗しているストリームだけ取り直す。 */
    data object RetryBackendData : ProfileIntent
    data object ConsumeEffect : ProfileIntent
}

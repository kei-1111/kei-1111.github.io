package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.Dp
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.mvi.Intent
import io.github.kei_1111.shared.model.LicenseEntry

internal sealed interface ProfileIntent : Intent {
    data class UpdateLayout(val layout: WindowLayout) : ProfileIntent
    data class UpdateSelectedPage(val page: EditorPage) : ProfileIntent
    data class UpdateSelectedPageFromTree(val page: EditorPage, val layout: WindowLayout) : ProfileIntent
    data class ClosePage(val page: EditorPage) : ProfileIntent
    data class ToggleTree(val layout: WindowLayout) : ProfileIntent
    data object ToggleLogcat : ProfileIntent
    data object ClearLogcat : ProfileIntent
    data class UpdateLogcatPanelHeight(val height: Dp) : ProfileIntent
    data class UpdateViewMode(val viewMode: EditorViewMode, val layout: WindowLayout) : ProfileIntent
    data class OpenUrl(val url: String) : ProfileIntent

    /** null = シート閉じる。 */
    data class UpdateSelectedLicense(val license: LicenseEntry?) : ProfileIntent
    data object ConsumeEffect : ProfileIntent
}

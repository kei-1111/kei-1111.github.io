package io.github.kei_1111.feature.profile.destination.profile

import io.github.kei_1111.core.designsystem.layout.WindowLayout
import io.github.kei_1111.core.mvi.Intent

internal sealed interface ProfileIntent : Intent {
    data class UpdateLayout(val layout: WindowLayout) : ProfileIntent
    data class UpdateSelectedPage(val page: EditorPage) : ProfileIntent
    data class UpdateSelectedPageFromTree(val page: EditorPage, val layout: WindowLayout) : ProfileIntent
    data class ToggleTree(val layout: WindowLayout) : ProfileIntent
    data class UpdateViewMode(val viewMode: EditorViewMode, val layout: WindowLayout) : ProfileIntent
    data class OpenUrl(val url: String) : ProfileIntent
    data object ConsumeEffect : ProfileIntent
}

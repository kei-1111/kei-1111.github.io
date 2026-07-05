package io.github.kei_1111.feature.profile.destination.profile

import io.github.kei_1111.core.mvi.Intent

internal sealed interface ProfileIntent : Intent {
    data class OnLayoutChanged(val layout: ProfileLayout) : ProfileIntent
    data class OnEditorTabClick(val page: EditorPage) : ProfileIntent
    data class OnTreeRowClick(val page: EditorPage, val layout: ProfileLayout) : ProfileIntent
    data class OnToggleTreeClick(val layout: ProfileLayout) : ProfileIntent
    data class OnViewModeSelect(val viewMode: EditorViewMode, val layout: ProfileLayout) : ProfileIntent
    data class OnUrlClick(val url: String) : ProfileIntent
    data object ConsumeEffect : ProfileIntent
}

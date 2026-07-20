package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import io.github.kei_1111.app.feature.profile.model.EditorPage

internal sealed interface SearchEverywhereEffect {
    data object NavigateBack : SearchEverywhereEffect
    data class ReturnPage(val page: EditorPage) : SearchEverywhereEffect
    data class OpenUrl(val url: String) : SearchEverywhereEffect
    data object ToggleTheme : SearchEverywhereEffect
}

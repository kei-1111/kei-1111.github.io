package io.github.kei_1111.app.feature.profile.destination.profile

internal sealed interface ProfileEffect {
    data object NavigateSearchEverywhere : ProfileEffect
    data class OpenUrl(val url: String) : ProfileEffect
}

package io.github.kei_1111.feature.profile.destination.profile

internal sealed interface ProfileEffect {
    data class OpenUrl(val url: String) : ProfileEffect
}

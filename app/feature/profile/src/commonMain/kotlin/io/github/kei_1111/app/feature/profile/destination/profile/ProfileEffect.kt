package io.github.kei_1111.app.feature.profile.destination.profile

import io.github.kei_1111.app.core.designsystem.language.KeiLanguage

internal sealed interface ProfileEffect {
    data object NavigateSearchEverywhere : ProfileEffect
    data class OpenUrl(val url: String) : ProfileEffect

    /** Terminal の theme コマンド。テーマ状態は App が所有するため、目標値を運んで Root 側で切り替える。 */
    data class SwitchTheme(val isDark: Boolean) : ProfileEffect

    /** Terminal の lang コマンド。言語状態は KeiLanguageController が所有するため、目標値を運ぶ。 */
    data class SwitchLanguage(val language: KeiLanguage) : ProfileEffect
}

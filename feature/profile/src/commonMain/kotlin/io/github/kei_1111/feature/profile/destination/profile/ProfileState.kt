package io.github.kei_1111.feature.profile.destination.profile

import io.github.kei_1111.core.model.ContributionCalendar
import io.github.kei_1111.core.model.GitHubProfile
import io.github.kei_1111.core.mvi.State

internal data class ProfileState(
    val selectedPage: EditorPage = EditorPage.Profile,
    val desktopTreeOpen: Boolean = true,
    val desktopViewMode: EditorViewMode = EditorViewMode.Split,
    val mobileTreeOpen: Boolean = false,
    val mobileViewMode: EditorViewMode = EditorViewMode.PreviewOnly,
    val profile: GitHubProfile? = null,
    val contributions: ContributionCalendar? = null,
    val effect: ProfileEffect? = null,
) : State

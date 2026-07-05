package io.github.kei_1111.feature.profile.destination.profile

import io.github.kei_1111.core.common.result.Result
import io.github.kei_1111.core.model.ContributionCalendar
import io.github.kei_1111.core.model.GitHubProfile
import io.github.kei_1111.core.mvi.ViewModelState

internal data class ProfileViewModelState(
    val selectedPage: EditorPage = EditorPage.Profile,
    val desktopTreeOpen: Boolean = true,
    val desktopViewMode: EditorViewMode = EditorViewMode.Split,
    val mobileTreeOpen: Boolean = false,
    val mobileViewMode: EditorViewMode = EditorViewMode.PreviewOnly,
    val currentLayout: ProfileLayout? = null,
    val profile: GitHubProfile? = null,
    val contributionsResult: Result<ContributionCalendar> = Result.Loading,
    val effect: ProfileEffect? = null,
) : ViewModelState<ProfileState> {
    override fun toState() = ProfileState(
        selectedPage = selectedPage,
        desktopTreeOpen = desktopTreeOpen,
        desktopViewMode = desktopViewMode,
        mobileTreeOpen = mobileTreeOpen,
        mobileViewMode = mobileViewMode,
        profile = profile,
        contributions = (contributionsResult as? Result.Success<ContributionCalendar>)?.data,
        effect = effect,
    )
}

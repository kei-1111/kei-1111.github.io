package io.github.kei_1111.app.feature.profile.destination.profile

import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.mvi.ViewModelState
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile

internal data class ProfileViewModelState(
    val selectedPage: EditorPage = EditorPage.Profile,
    val desktopTreeOpen: Boolean = true,
    val desktopViewMode: EditorViewMode = EditorViewMode.Split,
    val mobileTreeOpen: Boolean = false,
    val mobileViewMode: EditorViewMode = EditorViewMode.PreviewOnly,
    val currentLayout: WindowLayout? = null,
    val profileResult: Result<GitHubProfile> = Result.Loading,
    val contributionsResult: Result<ContributionCalendar> = Result.Loading,
    val effect: ProfileEffect? = null,
) : ViewModelState<ProfileState> {
    override fun toState() = ProfileState(
        selectedPage = selectedPage,
        desktopTreeOpen = desktopTreeOpen,
        desktopViewMode = desktopViewMode,
        mobileTreeOpen = mobileTreeOpen,
        mobileViewMode = mobileViewMode,
        profile = (profileResult as? Result.Success<GitHubProfile>)?.data,
        contributions = (contributionsResult as? Result.Success<ContributionCalendar>)?.data,
        effect = effect,
    )
}

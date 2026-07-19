package io.github.kei_1111.app.feature.profile.destination.profile

import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.mvi.ViewModelState
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ProfileViewModelState(
    val selectedPage: EditorPage? = EditorPage.Readme,
    val openPages: ImmutableList<EditorPage> = persistentListOf(EditorPage.Readme),
    val desktopTreeOpen: Boolean = true,
    val desktopViewMode: EditorViewMode = EditorViewMode.Split,
    val mobileTreeOpen: Boolean = false,
    val mobileViewMode: EditorViewMode = EditorViewMode.PreviewOnly,
    val currentLayout: WindowLayout? = null,
    val profileResult: Result<GitHubProfile> = Result.Loading,
    val contributionsResult: Result<ContributionCalendar> = Result.Loading,
    val licensesResult: Result<ThirdPartyLicenses> = Result.Loading,
    val selectedLicense: LicenseEntry? = null,
    val effect: ProfileEffect? = null,
) : ViewModelState<ProfileState> {
    override fun toState() = ProfileState(
        selectedPage = selectedPage,
        openPages = openPages,
        desktopTreeOpen = desktopTreeOpen,
        desktopViewMode = desktopViewMode,
        mobileTreeOpen = mobileTreeOpen,
        mobileViewMode = mobileViewMode,
        profile = (profileResult as? Result.Success<GitHubProfile>)?.data,
        contributions = (contributionsResult as? Result.Success<ContributionCalendar>)?.data,
        licenses = (licensesResult as? Result.Success<ThirdPartyLicenses>)?.data,
        selectedLicense = selectedLicense,
        effect = effect,
    )
}

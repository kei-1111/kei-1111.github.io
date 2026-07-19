package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.Dp
import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.mvi.ViewModelState
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
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
    /** ツリーと違いレイアウト非依存。ブレークポイントを跨いでも開閉状態を維持する。 */
    val logcatOpen: Boolean = false,
    /** Logcat パネルの高さ。開閉状態と同様レイアウト非依存で、ドラッグリサイズの結果を保持する。 */
    val logcatPanelHeight: Dp = ProfileDimensions.LogcatPanelHeight,
    val logEntries: ImmutableList<LogEntry> = persistentListOf(),
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
        logcatOpen = logcatOpen,
        logcatPanelHeight = logcatPanelHeight,
        logEntries = logEntries,
        profile = (profileResult as? Result.Success<GitHubProfile>)?.data,
        contributions = (contributionsResult as? Result.Success<ContributionCalendar>)?.data,
        licenses = (licensesResult as? Result.Success<ThirdPartyLicenses>)?.data,
        selectedLicense = selectedLicense,
        effect = effect,
    )
}

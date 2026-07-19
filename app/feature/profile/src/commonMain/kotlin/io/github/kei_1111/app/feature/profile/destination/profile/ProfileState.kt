package io.github.kei_1111.app.feature.profile.destination.profile

import io.github.kei_1111.app.core.mvi.State
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ProfileState(
    val selectedPage: EditorPage = EditorPage.Readme,
    /** エディタで開いているタブ列（開いた順）。ProjectTree から開くと追加される。 */
    val openPages: ImmutableList<EditorPage> = persistentListOf(EditorPage.Readme),
    val desktopTreeOpen: Boolean = true,
    val desktopViewMode: EditorViewMode = EditorViewMode.Split,
    val mobileTreeOpen: Boolean = false,
    val mobileViewMode: EditorViewMode = EditorViewMode.PreviewOnly,
    val profile: GitHubProfile? = null,
    val contributions: ContributionCalendar? = null,
    val licenses: ThirdPartyLicenses? = null,
    val profileEditorCode: String = "",
    val profileCodeError: Boolean = false,
    val editorResetTick: Int = 0,
    val selectedLicense: LicenseEntry? = null,
    val effect: ProfileEffect? = null,
) : State

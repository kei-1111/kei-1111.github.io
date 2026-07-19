package io.github.kei_1111.app.feature.profile.destination.profile

import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.mvi.ViewModelState
import io.github.kei_1111.app.feature.profile.destination.profile.component.ReadmeBlocks
import io.github.kei_1111.app.feature.profile.destination.profile.component.ReadmeSource
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownBlock
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorPage
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
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
    /** null = 未編集（生成コードを表示）。 */
    val editedProfileCode: String? = null,
    /** 最後にパース成功した編集結果。 */
    val parsedProfile: GitHubProfile? = null,
    val profileCodeError: Boolean = false,
    /** null = 未編集（生成 Markdown を表示）。 */
    val editedReadmeCode: String? = null,
    /** 最後にパースした README 編集結果。 */
    val parsedReadmeBlocks: ImmutableList<MarkdownBlock>? = null,
    /** リセット毎に増加し、エディタの TextFieldState を作り直す。 */
    val editorResetTick: Int = 0,
    val selectedLicense: LicenseEntry? = null,
    val effect: ProfileEffect? = null,
) : ViewModelState<ProfileState> {
    override fun toState(): ProfileState {
        val loadedProfile = (profileResult as? Result.Success<GitHubProfile>)?.data
        return ProfileState(
            selectedPage = selectedPage,
            openPages = openPages,
            desktopTreeOpen = desktopTreeOpen,
            desktopViewMode = desktopViewMode,
            mobileTreeOpen = mobileTreeOpen,
            mobileViewMode = mobileViewMode,
            profile = parsedProfile ?: loadedProfile,
            contributions = (contributionsResult as? Result.Success<ContributionCalendar>)?.data,
            licenses = (licensesResult as? Result.Success<ThirdPartyLicenses>)?.data,
            profileEditorCode = editedProfileCode ?: loadedProfile?.let(::profileCode).orEmpty(),
            readmeEditorCode = editedReadmeCode ?: ReadmeSource,
            readmeBlocks = parsedReadmeBlocks ?: ReadmeBlocks,
            profileCodeError = profileCodeError,
            editorResetTick = editorResetTick,
            selectedLicense = selectedLicense,
            effect = effect,
        )
    }
}

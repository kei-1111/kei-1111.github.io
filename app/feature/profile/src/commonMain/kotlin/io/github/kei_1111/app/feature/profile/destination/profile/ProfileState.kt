package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.Dp
import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.mvi.State
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ProfileState(
    /** 選択中のページ。openPages が空のときに限り null（全タブを閉じた状態）。 */
    val selectedPage: EditorPage? = EditorPage.Readme,
    /** エディタで開いているタブ列（開いた順）。ProjectTree から開くと追加される。 */
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
    val profile: GitHubProfile? = null,
    val contributions: ContributionCalendar? = null,
    val licenses: ThirdPartyLicenses? = null,
    val selectedLicense: LicenseEntry? = null,
    val effect: ProfileEffect? = null,
) : State

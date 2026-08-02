package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.common.logging.InteractionLog
import io.github.kei_1111.app.core.common.logging.LogLevel
import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.domain.usecase.GetContributionsUseCase
import io.github.kei_1111.app.core.domain.usecase.GetIssuesUseCase
import io.github.kei_1111.app.core.domain.usecase.GetLicensesUseCase
import io.github.kei_1111.app.core.testing.ViewModelTestBase
import io.github.kei_1111.app.core.testing.startCollecting
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownBlock
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownInline
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLineKind
import io.github.kei_1111.app.feature.profile.destination.profile.model.profileCode
import io.github.kei_1111.app.feature.profile.fake.FakeGetProfileUseCase
import io.github.kei_1111.app.feature.profile.fake.FakeGetWorksUseCase
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubIssue
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.LicenseType
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import io.github.kei_1111.shared.model.Work
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val PARSE_DEBOUNCE_MILLIS = 300L

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest : ViewModelTestBase() {

    @Test
    fun exposesProfileOnceLoaded() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = ProfileViewModel(
            fakeGetProfileUseCase,
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        val profile = testProfile()

        assertNull(viewModel.state.value.profile)

        fakeGetProfileUseCase.emit(profile)
        runCurrent()

        assertEquals(profile, viewModel.state.value.profile)
        assertFalse(viewModel.state.value.profileLoadFailed)
    }

    @Test
    fun flagsProfileLoadFailure() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = ProfileViewModel(
            fakeGetProfileUseCase,
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        fakeGetProfileUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.profile)
        assertTrue(viewModel.state.value.profileLoadFailed)
    }

    @Test
    fun exposesContributionsOnceLoaded() = runTest {
        val fakeGetContributionsUseCase = FakeGetContributionsUseCase()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            fakeGetContributionsUseCase,
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        val contributions = testContributions()

        assertNull(viewModel.state.value.contributions)

        fakeGetContributionsUseCase.emit(contributions)
        runCurrent()

        assertEquals(contributions, viewModel.state.value.contributions)
        assertFalse(viewModel.state.value.contributionsLoadFailed)
    }

    @Test
    fun flagsContributionsLoadFailure() = runTest {
        val fakeGetContributionsUseCase = FakeGetContributionsUseCase()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            fakeGetContributionsUseCase,
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        fakeGetContributionsUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.contributions)
        assertTrue(viewModel.state.value.contributionsLoadFailed)
    }

    @Test
    fun exposesLicensesOnceLoaded() = runTest {
        val fakeGetLicensesUseCase = FakeGetLicensesUseCase()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            fakeGetLicensesUseCase,
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        val licenses = testLicenses()

        assertNull(viewModel.state.value.licenses)

        fakeGetLicensesUseCase.emit(licenses)
        runCurrent()

        assertEquals(licenses, viewModel.state.value.licenses)
    }

    @Test
    fun keepsLicensesNullOnLoadFailure() = runTest {
        val fakeGetLicensesUseCase = FakeGetLicensesUseCase()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            fakeGetLicensesUseCase,
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        fakeGetLicensesUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.licenses)
        assertTrue(
            viewModel.state.value.logEntries.any { it.level == LogLevel.Error && it.tag == "Licenses" },
        )
    }

    @Test
    fun exposesIssuesOnceLoaded() = runTest {
        val fakeGetIssuesUseCase = FakeGetIssuesUseCase()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            fakeGetIssuesUseCase,
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        val issues = testIssues()

        assertNull(viewModel.state.value.issues)

        fakeGetIssuesUseCase.emit(issues)
        runCurrent()

        assertEquals(issues, viewModel.state.value.issues)
        assertFalse(viewModel.state.value.issuesLoadFailed)
    }

    @Test
    fun flagsIssuesLoadFailure() = runTest {
        val fakeGetIssuesUseCase = FakeGetIssuesUseCase()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            fakeGetIssuesUseCase,
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        fakeGetIssuesUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.issues)
        assertTrue(viewModel.state.value.issuesLoadFailed)
    }

    @Test
    fun exposesWorksOnceLoaded() = runTest {
        val fakeGetWorksUseCase = FakeGetWorksUseCase()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            fakeGetWorksUseCase,
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        val works = testWorks()

        assertNull(viewModel.state.value.works)

        fakeGetWorksUseCase.emit(works)
        runCurrent()

        assertEquals<List<Work>?>(works, viewModel.state.value.works)
        assertFalse(viewModel.state.value.worksLoadFailed)
    }

    @Test
    fun flagsWorksLoadFailure() = runTest {
        val fakeGetWorksUseCase = FakeGetWorksUseCase()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            fakeGetWorksUseCase,
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        fakeGetWorksUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.works)
        assertTrue(viewModel.state.value.worksLoadFailed)
    }

    @Test
    fun resetsMobileChromeWhenEnteringMobileLayout() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateLayout(WindowLayout.Desktop))
        runCurrent()
        viewModel.onIntent(ProfileIntent.ToggleTree(WindowLayout.Mobile))
        runCurrent()

        viewModel.onIntent(ProfileIntent.UpdateLayout(WindowLayout.Mobile))
        runCurrent()

        assertFalse(viewModel.state.value.mobileTreeOpen)
        assertEquals(EditorViewMode.PreviewOnly, viewModel.state.value.mobileViewMode)
    }

    @Test
    fun resetsDesktopChromeWhenEnteringDesktopLayout() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateLayout(WindowLayout.Mobile))
        runCurrent()
        viewModel.onIntent(ProfileIntent.ToggleTree(WindowLayout.Desktop))
        runCurrent()

        viewModel.onIntent(ProfileIntent.UpdateLayout(WindowLayout.Desktop))
        runCurrent()

        assertTrue(viewModel.state.value.desktopTreeOpen)
        assertEquals(EditorViewMode.Split, viewModel.state.value.desktopViewMode)
    }

    @Test
    fun ignoresResendOfCurrentLayout() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateLayout(WindowLayout.Desktop))
        runCurrent()
        viewModel.onIntent(ProfileIntent.ToggleTree(WindowLayout.Desktop))
        runCurrent()

        viewModel.onIntent(ProfileIntent.UpdateLayout(WindowLayout.Desktop))
        runCurrent()

        assertFalse(viewModel.state.value.desktopTreeOpen)
    }

    @Test
    fun growsOpenPagesOnceWhenOpeningFromTree() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Desktop))
        runCurrent()
        viewModel.onIntent(ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Desktop))
        runCurrent()

        assertEquals(persistentListOf(EditorPage.Readme, EditorPage.Profile), viewModel.state.value.openPages)
        assertEquals(EditorPage.Profile, viewModel.state.value.selectedPage)
    }

    @Test
    fun closesLicenseSheetOnSelectingDifferentPage() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateSelectedLicense(testLicenseEntry))
        runCurrent()

        viewModel.onIntent(ProfileIntent.UpdateSelectedPage(EditorPage.Profile))
        runCurrent()

        assertNull(viewModel.state.value.selectedLicense)
    }

    @Test
    fun keepsLicenseSheetOnReselectingSamePage() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateSelectedLicense(testLicenseEntry))
        runCurrent()

        viewModel.onIntent(ProfileIntent.UpdateSelectedPage(EditorPage.Readme))
        runCurrent()

        assertEquals(testLicenseEntry, viewModel.state.value.selectedLicense)
    }

    @Test
    fun autoClosesMobileTreeOnOpeningFromTree() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.ToggleTree(WindowLayout.Mobile))
        runCurrent()

        viewModel.onIntent(ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Mobile))
        runCurrent()

        assertFalse(viewModel.state.value.mobileTreeOpen)
    }

    @Test
    fun selectsRightNeighborOnClosingSelectedPage() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Desktop))
        runCurrent()
        viewModel.onIntent(ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Licenses, WindowLayout.Desktop))
        runCurrent()
        viewModel.onIntent(ProfileIntent.UpdateSelectedPage(EditorPage.Profile))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ClosePage(EditorPage.Profile))
        runCurrent()

        assertEquals(EditorPage.Licenses, viewModel.state.value.selectedPage)
        assertEquals(persistentListOf(EditorPage.Readme, EditorPage.Licenses), viewModel.state.value.openPages)
    }

    @Test
    fun selectsLeftNeighborOnClosingRightmostSelectedPage() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Desktop))
        runCurrent()
        viewModel.onIntent(ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Licenses, WindowLayout.Desktop))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ClosePage(EditorPage.Licenses))
        runCurrent()

        assertEquals(EditorPage.Profile, viewModel.state.value.selectedPage)
    }

    @Test
    fun clearsSelectionWhenClosingLastOpenPage() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.ClosePage(EditorPage.Readme))
        runCurrent()

        assertTrue(viewModel.state.value.openPages.isEmpty())
        assertNull(viewModel.state.value.selectedPage)
    }

    @Test
    fun ignoresClosingUnopenPage() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        val initialState = viewModel.state.value

        viewModel.onIntent(ProfileIntent.ClosePage(EditorPage.Profile))
        runCurrent()

        assertEquals(initialState, viewModel.state.value)
    }

    @Test
    fun keepsSelectionWhenClosingUnselectedPage() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Desktop))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ClosePage(EditorPage.Readme))
        runCurrent()

        assertEquals(EditorPage.Profile, viewModel.state.value.selectedPage)
        assertEquals(persistentListOf(EditorPage.Profile), viewModel.state.value.openPages)
    }

    @Test
    fun parsesProfileCodeAfterDebounce() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        val profile = roundTripProfile()

        viewModel.onIntent(ProfileIntent.UpdateProfileCode(profileCode(profile, KeiLanguage.Ja)))
        runCurrent()
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS - 1)
        runCurrent()

        assertNull(viewModel.state.value.profile)

        advanceTimeBy(1)
        runCurrent()

        assertEquals(profile, viewModel.state.value.profile)
        assertFalse(viewModel.state.value.profileCodeError)
    }

    @Test
    fun flagsProfileCodeErrorOnParseFailure() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.UpdateProfileCode("garbage"))
        runCurrent()
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS)
        runCurrent()

        assertTrue(viewModel.state.value.profileCodeError)
    }

    @Test
    fun parsesReadmeCodeAfterDebounce() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.UpdateReadmeCode("# Hello"))
        runCurrent()
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(
            MarkdownBlock.Heading(level = 1, inlines = listOf(MarkdownInline.PlainText("Hello"))),
            viewModel.state.value.readmeBlocks.first(),
        )
    }

    @Test
    fun restoresDefaultsOnResetEditorCode() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        val defaultReadmeCode = viewModel.state.value.readmeEditorCode
        viewModel.onIntent(ProfileIntent.UpdateProfileCode("garbage"))
        runCurrent()
        viewModel.onIntent(ProfileIntent.UpdateReadmeCode("# X"))
        runCurrent()
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS)
        runCurrent()

        viewModel.onIntent(ProfileIntent.ResetEditorCode)
        runCurrent()

        assertFalse(viewModel.state.value.profileCodeError)
        assertEquals(1, viewModel.state.value.profileEditorResetTick)
        assertEquals(1, viewModel.state.value.readmeEditorResetTick)
        assertEquals(defaultReadmeCode, viewModel.state.value.readmeEditorCode)
        assertTrue(viewModel.state.value.languageToggleEnabled)
    }

    @Test
    fun setsOpenUrlEffectOnOpenUrl() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.OpenUrl("https://example.com"))
        runCurrent()

        assertEquals(ProfileEffect.OpenUrl("https://example.com"), viewModel.state.value.effect)
    }

    @Test
    fun clearsEffectOnConsumeEffect() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.OpenUrl("https://example.com"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ConsumeEffect)
        runCurrent()

        assertNull(viewModel.state.value.effect)
    }

    @Test
    fun togglesTreeIndependentlyPerLayout() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.ToggleTree(WindowLayout.Desktop))
        runCurrent()

        assertFalse(viewModel.state.value.desktopTreeOpen)
        assertFalse(viewModel.state.value.mobileTreeOpen)

        viewModel.onIntent(ProfileIntent.ToggleTree(WindowLayout.Mobile))
        runCurrent()

        assertFalse(viewModel.state.value.desktopTreeOpen)
        assertTrue(viewModel.state.value.mobileTreeOpen)
    }

    @Test
    fun togglesLogcatOpen() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.ToggleLogcat)
        runCurrent()

        assertTrue(viewModel.state.value.logcatOpen)
    }

    @Test
    fun togglesTodoOpenAndClosesLogcat() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.ToggleLogcat)
        runCurrent()

        viewModel.onIntent(ProfileIntent.ToggleTodo)
        runCurrent()

        // 実 AS の下部ドックと同様、一度に開くツールウィンドウは 1 つ。
        assertTrue(viewModel.state.value.todoOpen)
        assertFalse(viewModel.state.value.logcatOpen)
    }

    @Test
    fun closesTodoOnOpeningLogcat() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.ToggleTodo)
        runCurrent()

        viewModel.onIntent(ProfileIntent.ToggleLogcat)
        runCurrent()

        assertTrue(viewModel.state.value.logcatOpen)
        assertFalse(viewModel.state.value.todoOpen)
    }

    @Test
    fun updatesTodoPanelHeight() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.UpdateTodoPanelHeight(320.dp))
        runCurrent()

        assertEquals(320.dp, viewModel.state.value.todoPanelHeight)
    }

    @Test
    fun retriesIssuesOnRetryGitHubData() = runTest {
        val fakeGetIssuesUseCase = FakeGetIssuesUseCase()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            fakeGetIssuesUseCase,
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        fakeGetIssuesUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertTrue(viewModel.state.value.issuesLoadFailed)

        // バックエンド回復を replay バッファの差し替えで模してから再試行する。
        fakeGetIssuesUseCase.emit(testIssues())
        runCurrent()

        viewModel.onIntent(ProfileIntent.RetryGitHubData)
        runCurrent()

        assertEquals(testIssues(), viewModel.state.value.issues)
        assertFalse(viewModel.state.value.issuesLoadFailed)
    }

    @Test
    fun retriesWorksOnRetryGitHubData() = runTest {
        val fakeGetWorksUseCase = FakeGetWorksUseCase()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            fakeGetWorksUseCase,
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        fakeGetWorksUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertTrue(viewModel.state.value.worksLoadFailed)

        // バックエンド回復を replay バッファの差し替えで模してから再試行する。
        fakeGetWorksUseCase.emit(testWorks())
        runCurrent()

        viewModel.onIntent(ProfileIntent.RetryGitHubData)
        runCurrent()

        assertEquals<List<Work>?>(testWorks(), viewModel.state.value.works)
        assertFalse(viewModel.state.value.worksLoadFailed)
    }

    @Test
    fun updatesAndClearsSelectedLicense() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.UpdateSelectedLicense(testLicenseEntry))
        runCurrent()

        assertEquals(testLicenseEntry, viewModel.state.value.selectedLicense)

        viewModel.onIntent(ProfileIntent.UpdateSelectedLicense(null))
        runCurrent()

        assertNull(viewModel.state.value.selectedLicense)
    }

    @Test
    fun opensTerminalAndClosesLogcatOnToggleTerminal() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.ToggleLogcat)
        runCurrent()

        viewModel.onIntent(ProfileIntent.ToggleTerminal)
        runCurrent()

        assertTrue(viewModel.state.value.terminalOpen)
        assertFalse(viewModel.state.value.logcatOpen)
    }

    @Test
    fun closesTerminalOnSecondToggle() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.ToggleTerminal)
        runCurrent()
        viewModel.onIntent(ProfileIntent.ToggleTerminal)
        runCurrent()

        assertFalse(viewModel.state.value.terminalOpen)
    }

    @Test
    fun closesTerminalOnOpeningLogcat() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.ToggleTerminal)
        runCurrent()

        viewModel.onIntent(ProfileIntent.ToggleLogcat)
        runCurrent()

        assertTrue(viewModel.state.value.logcatOpen)
        assertFalse(viewModel.state.value.terminalOpen)
    }

    @Test
    fun updatesTerminalInput() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("help"))
        runCurrent()

        assertEquals("help", viewModel.state.value.terminalInput)
    }

    @Test
    fun echoesPromptLineAndClearsInputOnExecuteCommand() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("help"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val echoLine = viewModel.state.value.terminalLines.first()
        assertEquals("kei@keis-macbook-pro kei-1111.github.io % help", echoLine.text)
        assertEquals(TerminalLineKind.Command, echoLine.kind)
        assertEquals("", viewModel.state.value.terminalInput)
    }

    @Test
    fun echoesBarePromptWithoutLoggingOnExecutingEmptyInput() = runTest {
        val interactionLog = InteractionLog()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            interactionLog,
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lines = viewModel.state.value.terminalLines
        assertEquals(1, lines.size)
        assertEquals("kei@keis-macbook-pro kei-1111.github.io %", lines.first().text)
        assertTrue(interactionLog.entries.value.none { it.tag == "Terminal" })
    }

    @Test
    fun recordsExecutedCommandToInteractionLog() = runTest {
        val interactionLog = InteractionLog()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            interactionLog,
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("help"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        assertTrue(
            interactionLog.entries.value.any { it.tag == "Terminal" && it.message == "run: help" },
        )
    }

    @Test
    fun printsCommandNotFoundOnUnknownCommand() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("foobar --baz"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("zsh: command not found: foobar", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun printsAvailableCommandsOnHelp() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("help"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lines = viewModel.state.value.terminalLines
        assertTrue(lines.none { it.kind == TerminalLineKind.Error })
        for (expected in listOf("help", "whoami", "ls", "open <target>", "theme dark|light", "lang en|ja", "./gradlew build")) {
            assertTrue(
                lines.any { it.kind == TerminalLineKind.Output && it.text.contains(expected) },
                "help output should mention $expected",
            )
        }
    }

    @Test
    fun listsProjectFilesOnLs() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("ls"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("README.md  ProfileScreen.kt  WorksScreen.kt  LicenseScreen.kt", lastLine.text)
        assertEquals(TerminalLineKind.Output, lastLine.kind)
    }

    @Test
    fun printsErrorOnWhoamiBeforeProfileLoads() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("whoami"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("whoami: profile not loaded yet", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun printsProfileSummaryOnWhoami() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = ProfileViewModel(
            fakeGetProfileUseCase,
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        fakeGetProfileUseCase.emit(testProfile())
        runCurrent()
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("whoami"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("kei-1111 — ケイ (Student, Tokyo)", lastLine.text)
        assertEquals(TerminalLineKind.Output, lastLine.kind)
    }

    @Test
    fun opensEditorPageOnOpenCommandWithAliasOrFileName() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("open profile"))
        runCurrent()
        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        assertEquals(EditorPage.Profile, viewModel.state.value.selectedPage)

        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("open LicenseScreen.kt"))
        runCurrent()
        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        assertEquals(EditorPage.Licenses, viewModel.state.value.selectedPage)
        assertEquals(
            persistentListOf(EditorPage.Readme, EditorPage.Profile, EditorPage.Licenses),
            viewModel.state.value.openPages,
        )
        assertEquals(TerminalLineKind.Command, viewModel.state.value.terminalLines.last().kind)
    }

    @Test
    fun setsOpenUrlEffectOnOpenLinkTarget() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = ProfileViewModel(
            fakeGetProfileUseCase,
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        fakeGetProfileUseCase.emit(
            testProfile(
                links = persistentListOf(
                    LinkService(type = LinkServiceType.GitHub, name = "GitHub", url = "https://github.com/kei-1111"),
                ),
            ),
        )
        runCurrent()
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("open github"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        assertEquals(ProfileEffect.OpenUrl("https://github.com/kei-1111"), viewModel.state.value.effect)
    }

    @Test
    fun printsErrorOnOpenLinkBeforeProfileLoads() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("open github"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("open: profile not loaded yet", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
        assertNull(viewModel.state.value.effect)
    }

    @Test
    fun printsErrorOnOpenWithUnknownTarget() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("open foo"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("open: no such target: foo", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun printsUsageOnOpenWithoutArgument() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("open"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("usage: open readme|profile|works|licenses|github|x|qiita|note", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun setsSwitchThemeEffectOnThemeCommandForDifferentTheme() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTheme(isDark = false))
        runCurrent()
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("theme dark"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        assertEquals(ProfileEffect.SwitchTheme(isDark = true), viewModel.state.value.effect)
        assertEquals("Switched to dark theme", viewModel.state.value.terminalLines.last().text)
    }

    @Test
    fun printsAlreadyMessageOnThemeCommandForCurrentTheme() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTheme(isDark = true))
        runCurrent()
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("theme dark"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        assertNull(viewModel.state.value.effect)
        assertEquals("theme: already dark", viewModel.state.value.terminalLines.last().text)
    }

    @Test
    fun printsUsageOnInvalidThemeArgument() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("theme blue"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("usage: theme dark|light", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun setsSwitchLanguageEffectOnLangCommandForDifferentLanguage() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("lang en"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        assertEquals(ProfileEffect.SwitchLanguage(KeiLanguage.En), viewModel.state.value.effect)
        assertEquals("Switched language to en", viewModel.state.value.terminalLines.last().text)
    }

    @Test
    fun printsAlreadyMessageOnLangCommandForCurrentLanguage() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("lang ja"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        assertNull(viewModel.state.value.effect)
        assertEquals("lang: already ja", viewModel.state.value.terminalLines.last().text)
    }

    @Test
    fun printsUsageOnInvalidLangArgument() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("lang fr"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("usage: lang en|ja", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun replaysBuildLogProgressivelyOnGradlewBuild() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("./gradlew build"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        // 実行直後はまだ完了行が出ていない（遅延つきで順次流れる）
        assertTrue(viewModel.state.value.terminalLines.none { it.text.contains("BUILD SUCCESSFUL") })

        advanceUntilIdle()

        val lines = viewModel.state.value.terminalLines
        assertTrue(lines.any { it.text == "Loading JetBrains Mono… done" })
        assertTrue(lines.any { it.text == "Rendering ProfilePreview… done" })
        val successLine = lines.single { it.text.startsWith("BUILD SUCCESSFUL") }
        assertEquals(TerminalLineKind.Success, successLine.kind)
    }

    @Test
    fun runsGradlewBuildDespiteIrregularWhitespace() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("./gradlew   build"))
        runCurrent()

        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.terminalLines.any { it.text.startsWith("BUILD SUCCESSFUL") })
    }

    @Test
    fun guardsConcurrentGradlewBuild() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("./gradlew build"))
        runCurrent()
        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        viewModel.onIntent(ProfileIntent.UpdateTerminalInput("./gradlew build"))
        runCurrent()
        viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
        runCurrent()

        val guardLine = viewModel.state.value.terminalLines.last()
        assertEquals("build already in progress", guardLine.text)
        assertEquals(TerminalLineKind.Error, guardLine.kind)

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.terminalLines.count { it.text.startsWith("BUILD SUCCESSFUL") })
    }

    @Test
    fun updatesTerminalPanelHeight() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        viewModel.onIntent(ProfileIntent.UpdateTerminalPanelHeight(320.dp))
        runCurrent()

        assertEquals(320.dp, viewModel.state.value.terminalPanelHeight)
    }

    @Test
    fun capsTerminalScrollbackAtLimit() = runTest {
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            InteractionLog(),
        )
        startCollecting(viewModel.state)

        repeat(250) {
            viewModel.onIntent(ProfileIntent.UpdateTerminalInput("ls"))
            runCurrent()
            viewModel.onIntent(ProfileIntent.ExecuteTerminalCommand)
            runCurrent()
        }

        // 250 回 × (エコー + 出力) = 500 行 → 上限 400 で古い行から捨てられる
        assertEquals(400, viewModel.state.value.terminalLines.size)
        assertEquals(
            "README.md  ProfileScreen.kt  WorksScreen.kt  LicenseScreen.kt",
            viewModel.state.value.terminalLines.last().text,
        )
    }

    @Test
    fun clearsLogEntriesOnClearLogcat() = runTest {
        val interactionLog = InteractionLog()
        val viewModel = ProfileViewModel(
            FakeGetProfileUseCase(),
            FakeGetContributionsUseCase(),
            FakeGetLicensesUseCase(),
            FakeGetIssuesUseCase(),
            FakeGetWorksUseCase(),
            interactionLog,
        )
        startCollecting(viewModel.state)
        viewModel.onIntent(ProfileIntent.OpenUrl("https://example.com"))
        runCurrent()

        assertTrue(viewModel.state.value.logEntries.isNotEmpty())

        viewModel.onIntent(ProfileIntent.ClearLogcat)
        runCurrent()

        assertTrue(viewModel.state.value.logEntries.isEmpty())
    }
}

private class FakeGetContributionsUseCase : GetContributionsUseCase {
    private val results = MutableSharedFlow<Result<ContributionCalendar>>(replay = 1)

    override fun invoke(): Flow<ContributionCalendar> = results.map { it.getOrThrow() }

    suspend fun emit(contributions: ContributionCalendar) = results.emit(Result.success(contributions))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

private class FakeGetIssuesUseCase : GetIssuesUseCase {
    private val results = MutableSharedFlow<Result<GitHubIssues>>(replay = 1)

    override fun invoke(): Flow<GitHubIssues> = results.map { it.getOrThrow() }

    suspend fun emit(issues: GitHubIssues) = results.emit(Result.success(issues))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

private class FakeGetLicensesUseCase : GetLicensesUseCase {
    private val results = MutableSharedFlow<Result<ThirdPartyLicenses>>(replay = 1)

    override fun invoke(): Flow<ThirdPartyLicenses> = results.map { it.getOrThrow() }

    suspend fun emit(licenses: ThirdPartyLicenses) = results.emit(Result.success(licenses))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

private val testLicenseEntry = LicenseEntry(
    name = "Library",
    owner = "Owner",
    type = LicenseType.Apache20,
    url = "https://example.com/library",
    copyright = "Copyright Owner",
)

private fun testProfile(
    links: ImmutableList<LinkService> = persistentListOf(),
) = GitHubProfile(
    name = LocalizedText(ja = "ケイ", en = "Kei"),
    handle = "kei-1111",
    location = "Tokyo",
    role = "Student",
    followers = 0,
    following = 0,
    repos = 0,
    totalStars = 0,
    pinnedRepos = persistentListOf(),
    languages = persistentListOf(),
    links = links,
)

private fun roundTripProfile() = GitHubProfile(
    name = LocalizedText(ja = "ケイ", en = "ケイ"),
    handle = "kei-1111",
    location = "Tokyo",
    role = "Student",
    followers = 12,
    following = 34,
    repos = 56,
    totalStars = 78,
    pinnedRepos = persistentListOf(),
    languages = persistentListOf(),
    links = persistentListOf(),
)

private fun testIssues() = GitHubIssues(
    totalCount = 1,
    issues = persistentListOf(
        GitHubIssue(
            number = 106,
            title = "Add a TODO tool window",
            url = "https://github.com/kei-1111/kei-1111.github.io/issues/106",
            type = "Feature",
        ),
    ),
)

private fun testContributions() = ContributionCalendar(
    totalLastYear = 0,
    days = persistentListOf(),
)

private fun testWorks() = listOf(
    Work(
        id = "kei-1111.github.io",
        name = "kei-1111.github.io",
        stack = "Kotlin · Compose Multiplatform",
        description = LocalizedText(ja = "ポートフォリオサイト", en = "Portfolio site"),
        tags = persistentListOf("Compose Multiplatform"),
        screenshots = persistentListOf(),
    ),
)

private fun testLicenses() = ThirdPartyLicenses(
    icons = persistentListOf(testLicenseEntry),
    fonts = persistentListOf(),
    app = persistentListOf(),
    server = persistentListOf(),
    texts = persistentMapOf(LicenseType.Apache20 to "Apache License 2.0"),
)

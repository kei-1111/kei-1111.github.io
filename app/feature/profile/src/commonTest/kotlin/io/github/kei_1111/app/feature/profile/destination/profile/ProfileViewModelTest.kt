package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.common.logging.InteractionLog
import io.github.kei_1111.app.core.common.logging.LogLevel
import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.domain.usecase.GetChangelogUseCase
import io.github.kei_1111.app.core.domain.usecase.GetContributionsUseCase
import io.github.kei_1111.app.core.domain.usecase.GetIssuesUseCase
import io.github.kei_1111.app.core.domain.usecase.GetLastNotifiedPrNumberUseCase
import io.github.kei_1111.app.core.domain.usecase.GetLicensesUseCase
import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.app.core.domain.usecase.GetReadmeUseCase
import io.github.kei_1111.app.core.domain.usecase.GetTerminalCommandsUseCase
import io.github.kei_1111.app.core.domain.usecase.GetWorksUseCase
import io.github.kei_1111.app.core.domain.usecase.SaveLastNotifiedPrNumberUseCase
import io.github.kei_1111.app.core.testing.ViewModelTestBase
import io.github.kei_1111.app.core.testing.dispatch
import io.github.kei_1111.app.core.testing.startCollecting
import io.github.kei_1111.app.feature.profile.destination.profile.model.BottomTool
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.model.ProfileBalloon
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLineKind
import io.github.kei_1111.app.feature.profile.destination.profile.model.profileCode
import io.github.kei_1111.app.feature.profile.destination.profile.model.worksCode
import io.github.kei_1111.app.feature.profile.fake.FakeGetProfileUseCase
import io.github.kei_1111.app.feature.profile.fake.FakeGetWorksUseCase
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubIssue
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.GitHubPullRequest
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.LicenseType
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.Profile
import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.TerminalTextCommand
import io.github.kei_1111.shared.model.TerminalTextCommands
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.WorkTag
import io.github.kei_1111.shared.model.Works
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
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
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
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
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)

        fakeGetProfileUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.profile)
        assertTrue(viewModel.state.value.profileLoadFailed)
    }

    @Test
    fun exposesContributionsOnceLoaded() = runTest {
        val fakeGetContributionsUseCase = FakeGetContributionsUseCase()
        val viewModel = createViewModel(getContributionsUseCase = fakeGetContributionsUseCase)
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
        val viewModel = createViewModel(getContributionsUseCase = fakeGetContributionsUseCase)
        startCollecting(viewModel.state)

        fakeGetContributionsUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.contributions)
        assertTrue(viewModel.state.value.contributionsLoadFailed)
    }

    @Test
    fun exposesLicensesOnceLoaded() = runTest {
        val fakeGetLicensesUseCase = FakeGetLicensesUseCase()
        val viewModel = createViewModel(getLicensesUseCase = fakeGetLicensesUseCase)
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
        val viewModel = createViewModel(getLicensesUseCase = fakeGetLicensesUseCase)
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
        val viewModel = createViewModel(getIssuesUseCase = fakeGetIssuesUseCase)
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
        val viewModel = createViewModel(getIssuesUseCase = fakeGetIssuesUseCase)
        startCollecting(viewModel.state)

        fakeGetIssuesUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.issues)
        assertTrue(viewModel.state.value.issuesLoadFailed)
    }

    @Test
    fun exposesWorksOnceLoaded() = runTest {
        val fakeGetWorksUseCase = FakeGetWorksUseCase()
        val viewModel = createViewModel(getWorksUseCase = fakeGetWorksUseCase)
        startCollecting(viewModel.state)
        val works = testWorks()

        assertNull(viewModel.state.value.works)

        fakeGetWorksUseCase.emit(works)
        runCurrent()

        assertEquals(works.items, viewModel.state.value.works)
        assertFalse(viewModel.state.value.worksLoadFailed)
    }

    @Test
    fun flagsWorksLoadFailure() = runTest {
        val fakeGetWorksUseCase = FakeGetWorksUseCase()
        val viewModel = createViewModel(getWorksUseCase = fakeGetWorksUseCase)
        startCollecting(viewModel.state)

        fakeGetWorksUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.works)
        assertTrue(viewModel.state.value.worksLoadFailed)
    }

    @Test
    fun resetsMobileChromeWhenEnteringMobileLayout() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateLayout(WindowLayout.Desktop))
        dispatch(viewModel, ProfileIntent.ToggleTree(WindowLayout.Mobile))

        dispatch(viewModel, ProfileIntent.UpdateLayout(WindowLayout.Mobile))

        assertFalse(viewModel.state.value.mobileTreeOpen)
        assertEquals(EditorViewMode.PreviewOnly, viewModel.state.value.mobileViewMode)
    }

    @Test
    fun resetsDesktopChromeWhenEnteringDesktopLayout() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateLayout(WindowLayout.Mobile))
        dispatch(viewModel, ProfileIntent.ToggleTree(WindowLayout.Desktop))

        dispatch(viewModel, ProfileIntent.UpdateLayout(WindowLayout.Desktop))

        assertTrue(viewModel.state.value.desktopTreeOpen)
        assertEquals(EditorViewMode.Split, viewModel.state.value.desktopViewMode)
    }

    @Test
    fun ignoresResendOfCurrentLayout() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateLayout(WindowLayout.Desktop))
        dispatch(viewModel, ProfileIntent.ToggleTree(WindowLayout.Desktop))

        dispatch(viewModel, ProfileIntent.UpdateLayout(WindowLayout.Desktop))

        assertFalse(viewModel.state.value.desktopTreeOpen)
    }

    @Test
    fun growsOpenPagesOnceWhenOpeningFromTree() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Desktop))
        dispatch(viewModel, ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Desktop))

        assertEquals(persistentListOf(EditorPage.Readme, EditorPage.Profile), viewModel.state.value.openPages)
        assertEquals(EditorPage.Profile, viewModel.state.value.selectedPage)
    }

    @Test
    fun closesLicenseSheetOnSelectingDifferentPage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateSelectedLicense(testLicenseEntry))

        dispatch(viewModel, ProfileIntent.UpdateSelectedPage(EditorPage.Profile))

        assertNull(viewModel.state.value.selectedLicense)
    }

    @Test
    fun keepsLicenseSheetOnReselectingSamePage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateSelectedLicense(testLicenseEntry))

        dispatch(viewModel, ProfileIntent.UpdateSelectedPage(EditorPage.Readme))

        assertEquals(testLicenseEntry, viewModel.state.value.selectedLicense)
    }

    @Test
    fun autoClosesMobileTreeOnOpeningFromTree() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.ToggleTree(WindowLayout.Mobile))

        dispatch(viewModel, ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Mobile))

        assertFalse(viewModel.state.value.mobileTreeOpen)
    }

    @Test
    fun selectsRightNeighborOnClosingSelectedPage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Desktop))
        dispatch(viewModel, ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Licenses, WindowLayout.Desktop))
        dispatch(viewModel, ProfileIntent.UpdateSelectedPage(EditorPage.Profile))

        dispatch(viewModel, ProfileIntent.ClosePage(EditorPage.Profile))

        assertEquals(EditorPage.Licenses, viewModel.state.value.selectedPage)
        assertEquals(persistentListOf(EditorPage.Readme, EditorPage.Licenses), viewModel.state.value.openPages)
    }

    @Test
    fun selectsLeftNeighborOnClosingRightmostSelectedPage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Desktop))
        dispatch(viewModel, ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Licenses, WindowLayout.Desktop))

        dispatch(viewModel, ProfileIntent.ClosePage(EditorPage.Licenses))

        assertEquals(EditorPage.Profile, viewModel.state.value.selectedPage)
    }

    @Test
    fun clearsSelectionWhenClosingLastOpenPage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.ClosePage(EditorPage.Readme))

        assertTrue(viewModel.state.value.openPages.isEmpty())
        assertNull(viewModel.state.value.selectedPage)
    }

    @Test
    fun ignoresClosingUnopenPage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        val initialState = viewModel.state.value

        dispatch(viewModel, ProfileIntent.ClosePage(EditorPage.Profile))

        assertEquals(initialState, viewModel.state.value)
    }

    @Test
    fun keepsSelectionWhenClosingUnselectedPage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateSelectedPageFromTree(EditorPage.Profile, WindowLayout.Desktop))

        dispatch(viewModel, ProfileIntent.ClosePage(EditorPage.Readme))

        assertEquals(EditorPage.Profile, viewModel.state.value.selectedPage)
        assertEquals(persistentListOf(EditorPage.Profile), viewModel.state.value.openPages)
    }

    @Test
    fun parsesProfileCodeAfterDebounce() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        val profile = roundTripProfile()

        dispatch(viewModel, ProfileIntent.UpdateProfileCode(profileCode(profile, KeiLanguage.Ja)))
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS - 1)
        runCurrent()

        assertNull(viewModel.state.value.profile)

        advanceTimeBy(1)
        runCurrent()

        assertEquals(profile, viewModel.state.value.profile)
        assertFalse(viewModel.state.value.profileCodeError)
    }

    @Test
    fun parsesProfileCodeAfterDebounceKeepingIconUrl() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)
        val profile = roundTripProfile().copy(iconUrl = "images/profile-icon.webp")
        fakeGetProfileUseCase.emit(profile)
        runCurrent()
        val edited = profileCode(profile, KeiLanguage.Ja)
            .replace("handle = \"kei-1111\"", "handle = \"renamed\"")

        dispatch(viewModel, ProfileIntent.UpdateProfileCode(edited))
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS)
        runCurrent()

        val parsedProfile = checkNotNull(viewModel.state.value.profile)
        assertEquals("renamed", parsedProfile.handle)
        assertEquals("images/profile-icon.webp", parsedProfile.iconUrl)
        assertFalse(viewModel.state.value.profileCodeError)
    }

    @Test
    fun leavesGeneratedCodeEmptyUntilTheLanguageArrives() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)

        fakeGetProfileUseCase.emit(roundTripProfile())
        runCurrent()

        assertEquals("", viewModel.state.value.profileEditorCode)
    }

    @Test
    fun omitsTheSwitchLogWhenTheLanguageFirstArrives() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateLanguage(KeiLanguage.Ja))

        assertFalse(viewModel.state.value.logEntries.any { it.tag == "Language" })
    }

    @Test
    fun logsTheSwitchWhenTheLanguageChangesAfterItArrived() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateLanguage(KeiLanguage.Ja))

        dispatch(viewModel, ProfileIntent.UpdateLanguage(KeiLanguage.En))

        assertTrue(
            viewModel.state.value.logEntries.any { it.level == LogLevel.Info && it.tag == "Language" },
        )
    }

    @Test
    fun resetsUneditedEditorBuffersWhenTheLanguageFirstArrives() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateLanguage(KeiLanguage.Ja))

        assertEquals(1, viewModel.state.value.profileEditorResetTick)
        assertEquals(1, viewModel.state.value.readmeEditorResetTick)
        assertEquals(1, viewModel.state.value.worksEditorResetTick)
    }

    @Test
    fun regeneratesUneditedCodeInTheNewLanguageOnUpdateLanguage() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)
        val profile = roundTripProfile()
        fakeGetProfileUseCase.emit(profile)
        runCurrent()

        dispatch(viewModel, ProfileIntent.UpdateLanguage(KeiLanguage.En))

        assertEquals(profileCode(profile, KeiLanguage.En), viewModel.state.value.profileEditorCode)
    }

    @Test
    fun keepsEditedCodeUntouchedOnUpdateLanguage() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)
        val profile = roundTripProfile()
        fakeGetProfileUseCase.emit(profile)
        runCurrent()
        val edited = profileCode(profile, KeiLanguage.Ja).replace("handle = \"kei-1111\"", "handle = \"renamed\"")
        dispatch(viewModel, ProfileIntent.UpdateProfileCode(edited))

        dispatch(viewModel, ProfileIntent.UpdateLanguage(KeiLanguage.En))

        assertEquals(edited, viewModel.state.value.profileEditorCode)
    }

    @Test
    fun flagsProfileCodeErrorOnParseFailure() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateProfileCode("garbage"))
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS)
        runCurrent()

        assertTrue(viewModel.state.value.profileCodeError)
    }

    @Test
    fun parsesReadmeCodeAfterDebounce() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateReadmeCode("# Hello"))
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(
            MarkdownBlock.Heading(level = 1, inlines = persistentListOf(MarkdownInline.PlainText("Hello"))),
            checkNotNull(viewModel.state.value.readmeBlocks).first(),
        )
    }

    @Test
    fun parsesEmptiedReadmeCodeToEmptyBlocksNotNull() = runTest {
        val fakeGetReadmeUseCase = FakeGetReadmeUseCase()
        val viewModel = createViewModel(getReadmeUseCase = fakeGetReadmeUseCase)
        startCollecting(viewModel.state)
        fakeGetReadmeUseCase.emit(testReadme())
        runCurrent()

        dispatch(viewModel, ProfileIntent.UpdateReadmeCode(""))
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS)
        runCurrent()

        val blocks = checkNotNull(viewModel.state.value.readmeBlocks)
        assertTrue(blocks.isEmpty())
    }

    @Test
    fun parsesWorksCodeAfterDebounceKeepingAssets() = runTest {
        val fakeGetWorksUseCase = FakeGetWorksUseCase()
        val viewModel = createViewModel(getWorksUseCase = fakeGetWorksUseCase)
        startCollecting(viewModel.state)
        fakeGetWorksUseCase.emit(testWorks())
        runCurrent()
        val edited = worksCode(testWorks().items, KeiLanguage.Ja)
            .replace("name = \"kei-1111.github.io\"", "name = \"renamed\"")

        dispatch(viewModel, ProfileIntent.UpdateWorksCode(edited))
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS)
        runCurrent()

        val works = checkNotNull(viewModel.state.value.works)
        assertEquals("renamed", works[0].name)
        assertEquals("kei-1111.github.io", works[0].id)
        assertFalse(viewModel.state.value.worksCodeError)
    }

    @Test
    fun flagsWorksCodeErrorOnParseFailure() = runTest {
        val fakeGetWorksUseCase = FakeGetWorksUseCase()
        val viewModel = createViewModel(getWorksUseCase = fakeGetWorksUseCase)
        startCollecting(viewModel.state)
        fakeGetWorksUseCase.emit(testWorks())
        runCurrent()

        dispatch(viewModel, ProfileIntent.UpdateWorksCode("garbage"))
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS)
        runCurrent()

        assertTrue(viewModel.state.value.worksCodeError)
        assertEquals(testWorks().items, viewModel.state.value.works)
    }

    @Test
    fun restoresDefaultsOnResetEditorCode() = runTest {
        val fakeGetReadmeUseCase = FakeGetReadmeUseCase()
        val viewModel = createViewModel(getReadmeUseCase = fakeGetReadmeUseCase)
        startCollecting(viewModel.state)
        fakeGetReadmeUseCase.emit(testReadme())
        runCurrent()
        val defaultReadmeCode = viewModel.state.value.readmeEditorCode
        dispatch(viewModel, ProfileIntent.UpdateProfileCode("garbage"))
        dispatch(viewModel, ProfileIntent.UpdateReadmeCode("# X"))
        dispatch(viewModel, ProfileIntent.UpdateWorksCode("garbage"))
        advanceTimeBy(PARSE_DEBOUNCE_MILLIS)
        runCurrent()

        dispatch(viewModel, ProfileIntent.ResetEditorCode)

        assertFalse(viewModel.state.value.profileCodeError)
        assertFalse(viewModel.state.value.worksCodeError)
        assertEquals(1, viewModel.state.value.profileEditorResetTick)
        assertEquals(1, viewModel.state.value.readmeEditorResetTick)
        assertEquals(1, viewModel.state.value.worksEditorResetTick)
        assertEquals(defaultReadmeCode, viewModel.state.value.readmeEditorCode)
        assertTrue(viewModel.state.value.languageToggleEnabled)
    }

    @Test
    fun setsOpenUrlEffectOnOpenUrl() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.OpenUrl("https://example.com"))

        assertEquals(ProfileEffect.OpenUrl("https://example.com"), viewModel.state.value.effect)
    }

    @Test
    fun clearsEffectOnConsumeEffect() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.OpenUrl("https://example.com"))

        dispatch(viewModel, ProfileIntent.ConsumeEffect)

        assertNull(viewModel.state.value.effect)
    }

    @Test
    fun togglesTreeIndependentlyPerLayout() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.ToggleTree(WindowLayout.Desktop))

        assertFalse(viewModel.state.value.desktopTreeOpen)
        assertFalse(viewModel.state.value.mobileTreeOpen)

        dispatch(viewModel, ProfileIntent.ToggleTree(WindowLayout.Mobile))

        assertFalse(viewModel.state.value.desktopTreeOpen)
        assertTrue(viewModel.state.value.mobileTreeOpen)
    }

    @Test
    fun togglesLogcatOpen() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.ToggleLogcat)

        assertEquals(BottomTool.Logcat, viewModel.state.value.openBottomTool)
    }

    @Test
    fun togglesTodoOpenAndClosesLogcat() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.ToggleLogcat)

        dispatch(viewModel, ProfileIntent.ToggleTodo)

        // 実 AS の下部ドックと同様、一度に開くツールウィンドウは 1 つ。
        assertEquals(BottomTool.Todo, viewModel.state.value.openBottomTool)
    }

    @Test
    fun closesTodoOnOpeningLogcat() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.ToggleTodo)

        dispatch(viewModel, ProfileIntent.ToggleLogcat)

        assertEquals(BottomTool.Logcat, viewModel.state.value.openBottomTool)
    }

    @Test
    fun updatesTodoPanelHeight() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateTodoPanelHeight(320.dp))

        assertEquals(320.dp, viewModel.state.value.todoPanelHeight)
    }

    @Test
    fun retriesIssuesOnRetryBackendData() = runTest {
        val fakeGetIssuesUseCase = FakeGetIssuesUseCase()
        val viewModel = createViewModel(getIssuesUseCase = fakeGetIssuesUseCase)
        startCollecting(viewModel.state)
        fakeGetIssuesUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertTrue(viewModel.state.value.issuesLoadFailed)

        // バックエンド回復を replay バッファの差し替えで模してから再試行する。
        fakeGetIssuesUseCase.emit(testIssues())
        runCurrent()

        dispatch(viewModel, ProfileIntent.RetryBackendData)

        assertEquals(testIssues(), viewModel.state.value.issues)
        assertFalse(viewModel.state.value.issuesLoadFailed)
    }

    @Test
    fun retriesWorksOnRetryBackendData() = runTest {
        val fakeGetWorksUseCase = FakeGetWorksUseCase()
        val viewModel = createViewModel(getWorksUseCase = fakeGetWorksUseCase)
        startCollecting(viewModel.state)
        fakeGetWorksUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertTrue(viewModel.state.value.worksLoadFailed)

        // バックエンド回復を replay バッファの差し替えで模してから再試行する。
        fakeGetWorksUseCase.emit(testWorks())
        runCurrent()

        dispatch(viewModel, ProfileIntent.RetryBackendData)

        assertEquals(testWorks().items, viewModel.state.value.works)
        assertFalse(viewModel.state.value.worksLoadFailed)
    }

    @Test
    fun exposesReadmeOnceLoaded() = runTest {
        val fakeGetReadmeUseCase = FakeGetReadmeUseCase()
        val viewModel = createViewModel(getReadmeUseCase = fakeGetReadmeUseCase)
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateLanguage(KeiLanguage.Ja))

        assertNull(viewModel.state.value.readmeBlocks)
        assertEquals("", viewModel.state.value.readmeEditorCode)

        fakeGetReadmeUseCase.emit(testReadme())
        runCurrent()

        assertEquals(testReadme().ja, viewModel.state.value.readmeBlocks)
        assertEquals("# こんにちは", viewModel.state.value.readmeEditorCode)
        assertFalse(viewModel.state.value.readmeLoadFailed)
    }

    @Test
    fun flagsReadmeLoadFailure() = runTest {
        val fakeGetReadmeUseCase = FakeGetReadmeUseCase()
        val viewModel = createViewModel(getReadmeUseCase = fakeGetReadmeUseCase)
        startCollecting(viewModel.state)

        fakeGetReadmeUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.readmeBlocks)
        assertTrue(viewModel.state.value.readmeLoadFailed)
    }

    @Test
    fun retriesReadmeOnRetryBackendData() = runTest {
        val fakeGetReadmeUseCase = FakeGetReadmeUseCase()
        val viewModel = createViewModel(getReadmeUseCase = fakeGetReadmeUseCase)
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateLanguage(KeiLanguage.Ja))
        fakeGetReadmeUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertTrue(viewModel.state.value.readmeLoadFailed)

        // バックエンド回復を replay バッファの差し替えで模してから再試行する。
        fakeGetReadmeUseCase.emit(testReadme())
        runCurrent()

        dispatch(viewModel, ProfileIntent.RetryBackendData)

        assertEquals(testReadme().ja, viewModel.state.value.readmeBlocks)
        assertFalse(viewModel.state.value.readmeLoadFailed)
    }

    @Test
    fun updatesAndClearsSelectedLicense() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateSelectedLicense(testLicenseEntry))

        assertEquals(testLicenseEntry, viewModel.state.value.selectedLicense)

        dispatch(viewModel, ProfileIntent.UpdateSelectedLicense(null))

        assertNull(viewModel.state.value.selectedLicense)
    }

    @Test
    fun opensAndClosesWorksSheet() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateWorksSheetVisibility(true))

        assertTrue(viewModel.state.value.worksSheetOpen)

        dispatch(viewModel, ProfileIntent.UpdateWorksSheetVisibility(false))

        assertFalse(viewModel.state.value.worksSheetOpen)
    }

    @Test
    fun closesWorksSheetOnSelectingDifferentPage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateWorksSheetVisibility(true))

        dispatch(viewModel, ProfileIntent.UpdateSelectedPage(EditorPage.Profile))

        assertFalse(viewModel.state.value.worksSheetOpen)
    }

    @Test
    fun keepsWorksSheetOnReselectingSamePage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateWorksSheetVisibility(true))

        dispatch(viewModel, ProfileIntent.UpdateSelectedPage(EditorPage.Readme))

        assertTrue(viewModel.state.value.worksSheetOpen)
    }

    @Test
    fun opensTerminalAndClosesLogcatOnToggleTerminal() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.ToggleLogcat)

        dispatch(viewModel, ProfileIntent.ToggleTerminal)

        assertEquals(BottomTool.Terminal, viewModel.state.value.openBottomTool)
    }

    @Test
    fun closesTerminalOnSecondToggle() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.ToggleTerminal)
        dispatch(viewModel, ProfileIntent.ToggleTerminal)

        assertNull(viewModel.state.value.openBottomTool)
    }

    @Test
    fun closesTerminalOnOpeningLogcat() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.ToggleTerminal)

        dispatch(viewModel, ProfileIntent.ToggleLogcat)

        assertEquals(BottomTool.Logcat, viewModel.state.value.openBottomTool)
    }

    @Test
    fun updatesTerminalInput() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("help"))

        assertEquals("help", viewModel.state.value.terminalInput)
    }

    @Test
    fun echoesPromptLineAndClearsInputOnExecuteCommand() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("help"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val echoLine = viewModel.state.value.terminalLines.first()
        assertEquals("kei@keis-macbook-pro kei-1111.github.io % help", echoLine.text)
        assertEquals(TerminalLineKind.Command, echoLine.kind)
        assertEquals("", viewModel.state.value.terminalInput)
    }

    @Test
    fun echoesBarePromptWithoutLoggingOnExecutingEmptyInput() = runTest {
        val interactionLog = InteractionLog()
        val viewModel = createViewModel(interactionLog = interactionLog)
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lines = viewModel.state.value.terminalLines
        assertEquals(1, lines.size)
        assertEquals("kei@keis-macbook-pro kei-1111.github.io %", lines.first().text)
        assertTrue(interactionLog.entries.value.none { it.tag == "Terminal" })
    }

    @Test
    fun recordsExecutedCommandToInteractionLog() = runTest {
        val interactionLog = InteractionLog()
        val viewModel = createViewModel(interactionLog = interactionLog)
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("help"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        assertTrue(
            interactionLog.entries.value.any { it.tag == "Terminal" && it.message == "run: help" },
        )
    }

    @Test
    fun printsCommandNotFoundOnUnknownCommand() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("foobar --baz"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("zsh: command not found: foobar", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun printsServerCommandLinesOnServerDefinedCommand() = runTest {
        val fakeGetTerminalCommandsUseCase = FakeGetTerminalCommandsUseCase()
        val viewModel = createViewModel(getTerminalCommandsUseCase = fakeGetTerminalCommandsUseCase)
        startCollecting(viewModel.state)
        fakeGetTerminalCommandsUseCase.emit(
            testTerminalCommands(lines = persistentListOf("first line", "second line")),
        )
        runCurrent()
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("neofetch"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        assertEquals(
            listOf(
                "kei@keis-macbook-pro kei-1111.github.io % neofetch",
                "first line",
                "second line",
            ),
            viewModel.state.value.terminalLines.map { it.text },
        )
        assertEquals(
            listOf(TerminalLineKind.Command, TerminalLineKind.Output, TerminalLineKind.Output),
            viewModel.state.value.terminalLines.map { it.kind },
        )
    }

    @Test
    fun listsServerCommandsInHelpOutput() = runTest {
        val fakeGetTerminalCommandsUseCase = FakeGetTerminalCommandsUseCase()
        val viewModel = createViewModel(getTerminalCommandsUseCase = fakeGetTerminalCommandsUseCase)
        startCollecting(viewModel.state)
        fakeGetTerminalCommandsUseCase.emit(testTerminalCommands())
        runCurrent()
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("help"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("  neofetch          show portfolio system info", lastLine.text)
        assertEquals(TerminalLineKind.Output, lastLine.kind)
    }

    @Test
    fun executesBuiltinWhenServerCommandUsesReservedKeyword() = runTest {
        val fakeGetTerminalCommandsUseCase = FakeGetTerminalCommandsUseCase()
        val viewModel = createViewModel(getTerminalCommandsUseCase = fakeGetTerminalCommandsUseCase)
        startCollecting(viewModel.state)
        fakeGetTerminalCommandsUseCase.emit(
            testTerminalCommands(
                keyword = "help",
                description = "shadow the builtin",
                lines = persistentListOf("shadowed output"),
            ),
        )
        runCurrent()
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("help"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val output = viewModel.state.value.terminalLines.drop(1)
        assertEquals("Available commands:", output.first().text)
        assertEquals("  ./gradlew build   run a build", output.last().text)
        assertTrue(output.none { it.text == "shadowed output" })
        assertTrue(output.none { it.text.contains("shadow the builtin") })
    }

    @Test
    fun printsCommandNotFoundWhenTerminalCommandsFetchFailed() = runTest {
        val fakeGetTerminalCommandsUseCase = FakeGetTerminalCommandsUseCase()
        val viewModel = createViewModel(getTerminalCommandsUseCase = fakeGetTerminalCommandsUseCase)
        startCollecting(viewModel.state)
        fakeGetTerminalCommandsUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("neofetch"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        assertEquals("zsh: command not found: neofetch", viewModel.state.value.terminalLines.last().text)

        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("ls"))
        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        assertEquals(
            "README.md  ProfileScreen.kt  WorksScreen.kt  LicenseScreen.kt",
            viewModel.state.value.terminalLines.last().text,
        )
        assertEquals(TerminalLineKind.Output, viewModel.state.value.terminalLines.last().kind)
    }

    @Test
    fun refetchesTerminalCommandsOnRetryBackendData() = runTest {
        val fakeGetTerminalCommandsUseCase = FakeGetTerminalCommandsUseCase()
        val viewModel = createViewModel(getTerminalCommandsUseCase = fakeGetTerminalCommandsUseCase)
        startCollecting(viewModel.state)
        fakeGetTerminalCommandsUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()
        fakeGetTerminalCommandsUseCase.emit(testTerminalCommands(lines = persistentListOf("retry succeeded")))
        runCurrent()

        dispatch(viewModel, ProfileIntent.RetryBackendData)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("neofetch"))
        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("retry succeeded", lastLine.text)
        assertEquals(TerminalLineKind.Output, lastLine.kind)
    }

    @Test
    fun printsAvailableCommandsOnHelp() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("help"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

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
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("ls"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("README.md  ProfileScreen.kt  WorksScreen.kt  LicenseScreen.kt", lastLine.text)
        assertEquals(TerminalLineKind.Output, lastLine.kind)
    }

    @Test
    fun printsErrorOnWhoamiBeforeProfileLoads() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("whoami"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("whoami: profile not loaded yet", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun printsProfileSummaryOnWhoami() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateLanguage(KeiLanguage.Ja))
        fakeGetProfileUseCase.emit(testProfile())
        runCurrent()
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("whoami"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("kei-1111 — ケイ (Student, Tokyo)", lastLine.text)
        assertEquals(TerminalLineKind.Output, lastLine.kind)
    }

    @Test
    fun opensEditorPageOnOpenCommandWithAliasOrFileName() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("open profile"))
        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        assertEquals(EditorPage.Profile, viewModel.state.value.selectedPage)

        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("open LicenseScreen.kt"))
        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

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
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)
        fakeGetProfileUseCase.emit(
            testProfile(
                links = persistentListOf(
                    LinkService(type = LinkServiceType.GitHub, name = "GitHub", url = "https://github.com/kei-1111"),
                ),
            ),
        )
        runCurrent()
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("open github"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        assertEquals(ProfileEffect.OpenUrl("https://github.com/kei-1111"), viewModel.state.value.effect)
    }

    @Test
    fun printsErrorOnOpenLinkBeforeProfileLoads() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("open github"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("open: profile not loaded yet", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
        assertNull(viewModel.state.value.effect)
    }

    @Test
    fun printsErrorOnOpenWithUnknownTarget() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("open foo"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("open: no such target: foo", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun printsUsageOnOpenWithoutArgument() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("open"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("usage: open readme|profile|works|licenses|github|x|qiita|note", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun setsSwitchThemeEffectOnThemeCommandForDifferentTheme() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTheme(isDark = false))
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("theme dark"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        assertEquals(ProfileEffect.SwitchTheme(isDark = true), viewModel.state.value.effect)
        assertEquals("Switched to dark theme", viewModel.state.value.terminalLines.last().text)
    }

    @Test
    fun printsAlreadyMessageOnThemeCommandForCurrentTheme() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTheme(isDark = true))
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("theme dark"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        assertNull(viewModel.state.value.effect)
        assertEquals("theme: already dark", viewModel.state.value.terminalLines.last().text)
    }

    @Test
    fun printsUsageOnInvalidThemeArgument() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("theme blue"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("usage: theme dark|light", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun setsSwitchLanguageEffectOnLangCommandForDifferentLanguage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("lang en"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        assertEquals(ProfileEffect.SwitchLanguage(KeiLanguage.En), viewModel.state.value.effect)
        assertEquals("Switched language to en", viewModel.state.value.terminalLines.last().text)
    }

    @Test
    fun printsAlreadyMessageOnLangCommandForCurrentLanguage() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateLanguage(KeiLanguage.Ja))
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("lang ja"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        assertNull(viewModel.state.value.effect)
        assertEquals("lang: already ja", viewModel.state.value.terminalLines.last().text)
    }

    @Test
    fun printsUsageOnInvalidLangArgument() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("lang fr"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val lastLine = viewModel.state.value.terminalLines.last()
        assertEquals("usage: lang en|ja", lastLine.text)
        assertEquals(TerminalLineKind.Error, lastLine.kind)
    }

    @Test
    fun replaysBuildLogProgressivelyOnGradlewBuild() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("./gradlew build"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

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
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("./gradlew   build"))

        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.terminalLines.any { it.text.startsWith("BUILD SUCCESSFUL") })
    }

    @Test
    fun guardsConcurrentGradlewBuild() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("./gradlew build"))
        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        dispatch(viewModel, ProfileIntent.UpdateTerminalInput("./gradlew build"))
        dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)

        val guardLine = viewModel.state.value.terminalLines.last()
        assertEquals("build already in progress", guardLine.text)
        assertEquals(TerminalLineKind.Error, guardLine.kind)

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.terminalLines.count { it.text.startsWith("BUILD SUCCESSFUL") })
    }

    @Test
    fun updatesTerminalPanelHeight() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateTerminalPanelHeight(320.dp))

        assertEquals(320.dp, viewModel.state.value.terminalPanelHeight)
    }

    @Test
    fun exposesChangelogOnceLoaded() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val viewModel = createViewModel(getChangelogUseCase = fakeGetChangelogUseCase)
        startCollecting(viewModel.state)

        assertNull(viewModel.state.value.changelog)

        fakeGetChangelogUseCase.emit(testChangelog())
        runCurrent()

        assertEquals(testChangelog(), viewModel.state.value.changelog)
        assertFalse(viewModel.state.value.changelogLoadFailed)
    }

    @Test
    fun flagsChangelogLoadFailure() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val viewModel = createViewModel(getChangelogUseCase = fakeGetChangelogUseCase)
        startCollecting(viewModel.state)

        fakeGetChangelogUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertNull(viewModel.state.value.changelog)
        assertTrue(viewModel.state.value.changelogLoadFailed)
    }

    @Test
    fun togglesChangelogOpenAndClosed() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.ToggleChangelog)

        assertEquals(BottomTool.Changelog, viewModel.state.value.openBottomTool)

        dispatch(viewModel, ProfileIntent.ToggleChangelog)

        assertNull(viewModel.state.value.openBottomTool)
    }

    @Test
    fun closesTerminalOnOpeningChangelog() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.ToggleTerminal)

        dispatch(viewModel, ProfileIntent.ToggleChangelog)

        assertEquals(BottomTool.Changelog, viewModel.state.value.openBottomTool)
    }

    @Test
    fun updatesChangelogPanelHeight() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, ProfileIntent.UpdateChangelogPanelHeight(320.dp))

        assertEquals(320.dp, viewModel.state.value.changelogPanelHeight)
    }

    @Test
    fun retriesChangelogOnRetryBackendData() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val viewModel = createViewModel(getChangelogUseCase = fakeGetChangelogUseCase)
        startCollecting(viewModel.state)
        fakeGetChangelogUseCase.emitFailure(IllegalStateException("boom"))
        runCurrent()

        assertTrue(viewModel.state.value.changelogLoadFailed)

        // バックエンド回復を replay バッファの差し替えで模してから再試行する。
        fakeGetChangelogUseCase.emit(testChangelog())
        runCurrent()

        dispatch(viewModel, ProfileIntent.RetryBackendData)

        assertEquals(testChangelog(), viewModel.state.value.changelog)
        assertFalse(viewModel.state.value.changelogLoadFailed)
    }

    @Test
    fun capsTerminalScrollbackAtLimit() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel.state)

        repeat(250) {
            dispatch(viewModel, ProfileIntent.UpdateTerminalInput("ls"))
            dispatch(viewModel, ProfileIntent.ExecuteTerminalCommand)
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
        val viewModel = createViewModel(interactionLog = interactionLog)
        startCollecting(viewModel.state)
        dispatch(viewModel, ProfileIntent.OpenUrl("https://example.com"))

        assertTrue(viewModel.state.value.logEntries.isNotEmpty())

        dispatch(viewModel, ProfileIntent.ClearLogcat)

        assertTrue(viewModel.state.value.logEntries.isEmpty())
    }

    @Test
    fun showsUpdateBalloonWithoutCountOnFirstVisit() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val fakeGetLastNotifiedPrNumberUseCase = FakeGetLastNotifiedPrNumberUseCase()
        val fakeSaveLastNotifiedPrNumberUseCase = FakeSaveLastNotifiedPrNumberUseCase()
        val viewModel = createViewModel(
            getChangelogUseCase = fakeGetChangelogUseCase,
            getLastNotifiedPrNumberUseCase = fakeGetLastNotifiedPrNumberUseCase,
            saveLastNotifiedPrNumberUseCase = fakeSaveLastNotifiedPrNumberUseCase,
        )
        startCollecting(viewModel.state)

        fakeGetChangelogUseCase.emit(changelogOf(206, 205))
        fakeGetLastNotifiedPrNumberUseCase.emit(null)
        runCurrent()

        assertEquals(
            persistentListOf(ProfileBalloon.SiteUpdated(newPullRequestCount = null)),
            viewModel.state.value.balloons,
        )
        assertEquals(206, fakeSaveLastNotifiedPrNumberUseCase.saved)
    }

    @Test
    fun showsUpdateBalloonWithNewPullRequestCountWhenAdvanced() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val fakeGetLastNotifiedPrNumberUseCase = FakeGetLastNotifiedPrNumberUseCase()
        val fakeSaveLastNotifiedPrNumberUseCase = FakeSaveLastNotifiedPrNumberUseCase()
        val viewModel = createViewModel(
            getChangelogUseCase = fakeGetChangelogUseCase,
            getLastNotifiedPrNumberUseCase = fakeGetLastNotifiedPrNumberUseCase,
            saveLastNotifiedPrNumberUseCase = fakeSaveLastNotifiedPrNumberUseCase,
        )
        startCollecting(viewModel.state)

        fakeGetChangelogUseCase.emit(changelogOf(208, 207, 206, 205))
        fakeGetLastNotifiedPrNumberUseCase.emit(206)
        runCurrent()

        assertEquals(
            persistentListOf(ProfileBalloon.SiteUpdated(newPullRequestCount = 2)),
            viewModel.state.value.balloons,
        )
        assertEquals(208, fakeSaveLastNotifiedPrNumberUseCase.saved)
    }

    @Test
    fun showsNoUpdateBalloonWhenPullRequestNumberDidNotAdvance() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val fakeGetLastNotifiedPrNumberUseCase = FakeGetLastNotifiedPrNumberUseCase()
        val fakeSaveLastNotifiedPrNumberUseCase = FakeSaveLastNotifiedPrNumberUseCase()
        val viewModel = createViewModel(
            getChangelogUseCase = fakeGetChangelogUseCase,
            getLastNotifiedPrNumberUseCase = fakeGetLastNotifiedPrNumberUseCase,
            saveLastNotifiedPrNumberUseCase = fakeSaveLastNotifiedPrNumberUseCase,
        )
        startCollecting(viewModel.state)

        fakeGetChangelogUseCase.emit(changelogOf(206, 205))
        fakeGetLastNotifiedPrNumberUseCase.emit(206)
        runCurrent()

        assertTrue(viewModel.state.value.balloons.isEmpty())
        assertNull(fakeSaveLastNotifiedPrNumberUseCase.saved)
    }

    @Test
    fun showsUpdateBalloonWhenPersistenceIsUnavailable() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val fakeGetLastNotifiedPrNumberUseCase = FakeGetLastNotifiedPrNumberUseCase()
        val viewModel = createViewModel(
            getChangelogUseCase = fakeGetChangelogUseCase,
            getLastNotifiedPrNumberUseCase = fakeGetLastNotifiedPrNumberUseCase,
        )
        startCollecting(viewModel.state)

        fakeGetChangelogUseCase.emit(changelogOf(206))
        fakeGetLastNotifiedPrNumberUseCase.emitFailure(IllegalStateException("storage unavailable"))
        runCurrent()

        assertEquals(
            persistentListOf(ProfileBalloon.SiteUpdated(newPullRequestCount = null)),
            viewModel.state.value.balloons,
        )
    }

    @Test
    fun showsNoUpdateBalloonWhenChangelogFails() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val fakeGetLastNotifiedPrNumberUseCase = FakeGetLastNotifiedPrNumberUseCase()
        val viewModel = createViewModel(
            getChangelogUseCase = fakeGetChangelogUseCase,
            getLastNotifiedPrNumberUseCase = fakeGetLastNotifiedPrNumberUseCase,
        )
        startCollecting(viewModel.state)

        fakeGetChangelogUseCase.emitFailure(IllegalStateException("changelog fetch failed"))
        fakeGetLastNotifiedPrNumberUseCase.emit(null)
        runCurrent()

        assertTrue(viewModel.state.value.balloons.isEmpty())
    }

    @Test
    fun showsFallbackWarningWhenProfileIsServedFromFallback() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)

        fakeGetProfileUseCase.emit(testProfile(statistics = null))
        runCurrent()

        assertEquals(persistentListOf(ProfileBalloon.FallbackWarning), viewModel.state.value.balloons)
    }

    @Test
    fun showsNoFallbackWarningWhenProfileIsFresh() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)

        fakeGetProfileUseCase.emit(testProfile())
        runCurrent()

        assertTrue(viewModel.state.value.balloons.isEmpty())
    }

    @Test
    fun dismissesOnlyTheRequestedBalloon() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val fakeGetLastNotifiedPrNumberUseCase = FakeGetLastNotifiedPrNumberUseCase()
        val viewModel = createViewModel(
            getProfileUseCase = fakeGetProfileUseCase,
            getChangelogUseCase = fakeGetChangelogUseCase,
            getLastNotifiedPrNumberUseCase = fakeGetLastNotifiedPrNumberUseCase,
        )
        startCollecting(viewModel.state)
        fakeGetProfileUseCase.emit(testProfile(statistics = null))
        runCurrent()
        fakeGetChangelogUseCase.emit(changelogOf(206))
        fakeGetLastNotifiedPrNumberUseCase.emit(null)
        runCurrent()

        assertEquals(2, viewModel.state.value.balloons.size)

        dispatch(viewModel, ProfileIntent.DismissBalloon(ProfileBalloon.SiteUpdated.ID))

        assertEquals(persistentListOf(ProfileBalloon.FallbackWarning), viewModel.state.value.balloons)
    }

    @Test
    fun opensTheGitToolWindowOnOpenChangelog() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val fakeGetLastNotifiedPrNumberUseCase = FakeGetLastNotifiedPrNumberUseCase()
        val viewModel = createViewModel(
            getChangelogUseCase = fakeGetChangelogUseCase,
            getLastNotifiedPrNumberUseCase = fakeGetLastNotifiedPrNumberUseCase,
        )
        startCollecting(viewModel.state)
        fakeGetChangelogUseCase.emit(changelogOf(206))
        fakeGetLastNotifiedPrNumberUseCase.emit(null)
        runCurrent()

        dispatch(viewModel, ProfileIntent.OpenChangelog)

        // バルーンの消滅は退出アニメーションを終えた KeiBalloon が DismissBalloon を投げて行う
        assertEquals(BottomTool.Changelog, viewModel.state.value.openBottomTool)
        assertEquals(
            persistentListOf(ProfileBalloon.SiteUpdated(newPullRequestCount = null)),
            viewModel.state.value.balloons,
        )
    }

    @Test
    fun showsUpdateBalloonUsingTheHighestPullRequestNumber() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val fakeGetLastNotifiedPrNumberUseCase = FakeGetLastNotifiedPrNumberUseCase()
        val fakeSaveLastNotifiedPrNumberUseCase = FakeSaveLastNotifiedPrNumberUseCase()
        val viewModel = createViewModel(
            getChangelogUseCase = fakeGetChangelogUseCase,
            getLastNotifiedPrNumberUseCase = fakeGetLastNotifiedPrNumberUseCase,
            saveLastNotifiedPrNumberUseCase = fakeSaveLastNotifiedPrNumberUseCase,
        )
        startCollecting(viewModel.state)

        // マージ順で並ぶため先頭が最大番号とは限らない
        fakeGetChangelogUseCase.emit(changelogOf(205, 207, 206))
        fakeGetLastNotifiedPrNumberUseCase.emit(206)
        runCurrent()

        assertEquals(
            persistentListOf(ProfileBalloon.SiteUpdated(newPullRequestCount = 1)),
            viewModel.state.value.balloons,
        )
        assertEquals(207, fakeSaveLastNotifiedPrNumberUseCase.saved)
    }

    @Test
    fun showsUpdateBalloonAfterAFailedChangelogFetchRecovers() = runTest {
        val fakeGetChangelogUseCase = FakeGetChangelogUseCase()
        val fakeGetLastNotifiedPrNumberUseCase = FakeGetLastNotifiedPrNumberUseCase()
        val viewModel = createViewModel(
            getChangelogUseCase = fakeGetChangelogUseCase,
            getLastNotifiedPrNumberUseCase = fakeGetLastNotifiedPrNumberUseCase,
        )
        startCollecting(viewModel.state)
        fakeGetChangelogUseCase.emitFailure(IllegalStateException("changelog fetch failed"))
        fakeGetLastNotifiedPrNumberUseCase.emit(null)
        runCurrent()

        assertTrue(viewModel.state.value.balloons.isEmpty())

        // バックエンド回復を replay バッファの差し替えで模してから再試行する。
        fakeGetChangelogUseCase.emit(changelogOf(206))
        runCurrent()
        dispatch(viewModel, ProfileIntent.RetryBackendData)

        assertEquals(
            persistentListOf(ProfileBalloon.SiteUpdated(newPullRequestCount = null)),
            viewModel.state.value.balloons,
        )
    }

    @Test
    fun showsFallbackWarningAfterAFailedProfileFetchRecovers() = runTest {
        val fakeGetProfileUseCase = FakeGetProfileUseCase()
        val viewModel = createViewModel(getProfileUseCase = fakeGetProfileUseCase)
        startCollecting(viewModel.state)
        fakeGetProfileUseCase.emitFailure(IllegalStateException("profile fetch failed"))
        runCurrent()

        assertTrue(viewModel.state.value.balloons.isEmpty())

        // バックエンド回復を replay バッファの差し替えで模してから再試行する。
        fakeGetProfileUseCase.emit(testProfile(statistics = null))
        runCurrent()
        dispatch(viewModel, ProfileIntent.RetryBackendData)

        assertEquals(persistentListOf(ProfileBalloon.FallbackWarning), viewModel.state.value.balloons)
    }
}

private fun createViewModel(
    getProfileUseCase: GetProfileUseCase = FakeGetProfileUseCase(),
    getContributionsUseCase: GetContributionsUseCase = FakeGetContributionsUseCase(),
    getLicensesUseCase: GetLicensesUseCase = FakeGetLicensesUseCase(),
    getIssuesUseCase: GetIssuesUseCase = FakeGetIssuesUseCase(),
    getWorksUseCase: GetWorksUseCase = FakeGetWorksUseCase(),
    getReadmeUseCase: GetReadmeUseCase = FakeGetReadmeUseCase(),
    getTerminalCommandsUseCase: GetTerminalCommandsUseCase = FakeGetTerminalCommandsUseCase(),
    getChangelogUseCase: GetChangelogUseCase = FakeGetChangelogUseCase(),
    getLastNotifiedPrNumberUseCase: GetLastNotifiedPrNumberUseCase = FakeGetLastNotifiedPrNumberUseCase(),
    saveLastNotifiedPrNumberUseCase: SaveLastNotifiedPrNumberUseCase = FakeSaveLastNotifiedPrNumberUseCase(),
    interactionLog: InteractionLog = InteractionLog(),
) = ProfileViewModel(
    getProfileUseCase,
    getContributionsUseCase,
    getLicensesUseCase,
    getIssuesUseCase,
    getWorksUseCase,
    getReadmeUseCase,
    getTerminalCommandsUseCase,
    getChangelogUseCase,
    getLastNotifiedPrNumberUseCase,
    saveLastNotifiedPrNumberUseCase,
    interactionLog,
)

private class FakeGetLastNotifiedPrNumberUseCase : GetLastNotifiedPrNumberUseCase {
    private val results = MutableSharedFlow<Result<Int?>>(replay = 1)

    override fun invoke(): Flow<Int?> = results.map { it.getOrThrow() }

    suspend fun emit(prNumber: Int?) = results.emit(Result.success(prNumber))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

private class FakeSaveLastNotifiedPrNumberUseCase : SaveLastNotifiedPrNumberUseCase {
    var saved: Int? = null
        private set

    override suspend fun invoke(prNumber: Int) {
        saved = prNumber
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

private class FakeGetReadmeUseCase : GetReadmeUseCase {
    private val results = MutableSharedFlow<Result<Readme>>(replay = 1)

    override fun invoke(): Flow<Readme> = results.map { it.getOrThrow() }

    suspend fun emit(readme: Readme) = results.emit(Result.success(readme))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

private class FakeGetLicensesUseCase : GetLicensesUseCase {
    private val results = MutableSharedFlow<Result<ThirdPartyLicenses>>(replay = 1)

    override fun invoke(): Flow<ThirdPartyLicenses> = results.map { it.getOrThrow() }

    suspend fun emit(licenses: ThirdPartyLicenses) = results.emit(Result.success(licenses))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

private class FakeGetChangelogUseCase : GetChangelogUseCase {
    private val results = MutableSharedFlow<Result<GitHubChangelog>>(replay = 1)

    override fun invoke(): Flow<GitHubChangelog> = results.map { it.getOrThrow() }

    suspend fun emit(changelog: GitHubChangelog) = results.emit(Result.success(changelog))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

private class FakeGetTerminalCommandsUseCase : GetTerminalCommandsUseCase {
    private val results = MutableSharedFlow<Result<TerminalTextCommands>>(replay = 1)

    override fun invoke(): Flow<TerminalTextCommands> = results.map { it.getOrThrow() }

    suspend fun emit(terminalCommands: TerminalTextCommands) = results.emit(Result.success(terminalCommands))

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
    statistics: Int? = 0,
) = Profile(
    name = LocalizedText(ja = "ケイ", en = "Kei"),
    handle = "kei-1111",
    location = "Tokyo",
    role = "Student",
    followers = statistics,
    following = statistics,
    repos = statistics,
    totalStars = statistics,
    pinnedRepos = persistentListOf(),
    languages = persistentListOf(),
    links = links,
)

private fun roundTripProfile() = Profile(
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

private fun testChangelog() = GitHubChangelog(
    pullRequests = persistentListOf(
        GitHubPullRequest(
            number = 204,
            title = "Introduce KeiAsyncImage",
            url = "https://github.com/kei-1111/kei-1111.github.io/pull/204",
            headRefName = "refactor/#201",
            mergedAt = "2026-08-09T06:02:11Z",
            type = "Refactor",
            author = "kei-1111",
        ),
    ),
)

private fun changelogOf(vararg numbers: Int) = GitHubChangelog(
    pullRequests = numbers.map { number ->
        GitHubPullRequest(
            number = number,
            title = "Pull request #$number",
            url = "https://github.com/kei-1111/kei-1111.github.io/pull/$number",
            headRefName = "feature/#$number",
            mergedAt = "2026-08-09T06:02:11Z",
            type = "Feature",
            author = "kei-1111",
        )
    }.toImmutableList(),
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

private fun testWorks() = Works(
    items = persistentListOf(
        Work(
            id = "kei-1111.github.io",
            name = "kei-1111.github.io",
            kind = "Portfolio Website",
            period = "2025–",
            description = LocalizedText(ja = "ポートフォリオサイト", en = "Portfolio site"),
            tags = persistentListOf(WorkTag(name = "Compose Multiplatform", accent = true)),
            screenshots = persistentListOf(),
        ),
    ),
)

private fun testTerminalCommands(
    keyword: String = "neofetch",
    description: String = "show portfolio system info",
    lines: ImmutableList<String> = persistentListOf("test output"),
) = TerminalTextCommands(
    items = persistentListOf(
        TerminalTextCommand(
            keyword = keyword,
            description = description,
            lines = lines,
        ),
    ),
)

private fun testReadme() = Readme(
    ja = persistentListOf(
        MarkdownBlock.Heading(level = 1, inlines = persistentListOf(MarkdownInline.PlainText("こんにちは"))),
    ),
    en = persistentListOf(
        MarkdownBlock.Heading(level = 1, inlines = persistentListOf(MarkdownInline.PlainText("Hello"))),
    ),
)

private fun testLicenses() = ThirdPartyLicenses(
    icons = persistentListOf(testLicenseEntry),
    fonts = persistentListOf(),
    app = persistentListOf(),
    server = persistentListOf(),
    texts = persistentMapOf(LicenseType.Apache20 to "Apache License 2.0"),
)

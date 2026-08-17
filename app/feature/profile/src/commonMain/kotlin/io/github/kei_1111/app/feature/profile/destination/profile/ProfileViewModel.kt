package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.kei_1111.app.core.common.logging.InteractionLog
import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.common.result.asResult
import io.github.kei_1111.app.core.common.result.successOrNull
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
import io.github.kei_1111.app.core.mvi.MviViewModel
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.parseMarkdown
import io.github.kei_1111.app.feature.profile.destination.profile.model.BottomTool
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.model.ProfileBalloon
import io.github.kei_1111.app.feature.profile.destination.profile.model.TERMINAL_BUILD_LOG_STEPS
import io.github.kei_1111.app.feature.profile.destination.profile.model.TERMINAL_MAX_LINES
import io.github.kei_1111.app.feature.profile.destination.profile.model.TERMINAL_PROMPT
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalCommand
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLine
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLineKind
import io.github.kei_1111.app.feature.profile.destination.profile.model.forLanguage
import io.github.kei_1111.app.feature.profile.destination.profile.model.overlayProfileAssets
import io.github.kei_1111.app.feature.profile.destination.profile.model.overlayWorksAssets
import io.github.kei_1111.app.feature.profile.destination.profile.model.parseProfileCode
import io.github.kei_1111.app.feature.profile.destination.profile.model.parseTerminalCommand
import io.github.kei_1111.app.feature.profile.destination.profile.model.parseWorksCode
import io.github.kei_1111.app.feature.profile.destination.profile.model.terminalHelpLines
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.Profile
import io.github.kei_1111.shared.model.hasGitHubStatistics
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

private const val PARSE_DEBOUNCE_MILLIS = 300L

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
// バックエンドデータはストリーム毎に load 関数を分ける設計（RetryBackendData が失敗分だけ取り直す）のため関数数が閾値に達する。
@Suppress("TooManyFunctions")
internal class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val getContributionsUseCase: GetContributionsUseCase,
    private val getLicensesUseCase: GetLicensesUseCase,
    private val getIssuesUseCase: GetIssuesUseCase,
    private val getWorksUseCase: GetWorksUseCase,
    private val getReadmeUseCase: GetReadmeUseCase,
    private val getTerminalCommandsUseCase: GetTerminalCommandsUseCase,
    private val getChangelogUseCase: GetChangelogUseCase,
    private val getLastNotifiedPrNumberUseCase: GetLastNotifiedPrNumberUseCase,
    private val saveLastNotifiedPrNumberUseCase: SaveLastNotifiedPrNumberUseCase,
    private val interactionLog: InteractionLog,
) : MviViewModel<ProfileViewModelState, ProfileState, ProfileIntent>() {

    override fun createInitialViewModelState() = ProfileViewModelState()

    // 初期 State は初期 ViewModelState から導出する。別々に組むと初回フレーム（ゲートなしで
    // 可視）と以降の VMS 由来 State がずれ、README エディタの TextFieldState が初期言語の
    // ソースで確定したまま UpdateLanguage の差分検知にも掛からない。
    override fun createInitialState() = createInitialViewModelState().toState()

    init {
        // README はランディングページの表示内容のため最初に発火する。
        loadReadme()
        loadProfile()
        loadContributions()
        loadLicenses()
        loadIssues()
        loadWorks()
        loadTerminalCommands()
        loadChangelog()
        observeUpdateNotice()
        observeFallbackWarning()
        observeProfileCode()
        observeReadmeCode()
        observeWorksCode()
        observeInteractionLog()
    }

    private fun loadProfile() = getProfileUseCase().collectAsResult { copy(profileResult = it) }

    private fun loadLicenses() {
        viewModelScope.launch {
            getLicensesUseCase().asResult().collect { result ->
                if (result is Result.Error) {
                    interactionLog.e("Licenses", "failed to load third-party licenses")
                }
                updateViewModelState { copy(licensesResult = result) }
            }
        }
    }

    private fun loadContributions() =
        getContributionsUseCase().collectAsResult { copy(contributionsResult = it) }

    private fun loadIssues() = getIssuesUseCase().collectAsResult { copy(issuesResult = it) }

    private fun loadWorks() = getWorksUseCase().collectAsResult { copy(worksResult = it) }

    private fun loadReadme() = getReadmeUseCase().collectAsResult { copy(readmeResult = it) }

    private fun loadTerminalCommands() =
        getTerminalCommandsUseCase().collectAsResult { copy(terminalCommandsResult = it) }

    private fun loadChangelog() = getChangelogUseCase().collectAsResult { copy(changelogResult = it) }

    // チェンジログの最大 PR 番号を訪問カーソルとして保存し、進んでいれば更新を知らせる（時刻は持たない）。
    // 一覧はマージ順で番号順ではないため先頭ではなく最大番号を採るが、番号は作成時の採番なので
    // 通知済みより小さい番号が後からマージされるとその 1 件は通知しそこねる。
    // 保存が読めない場合は初回訪問として扱う。
    private fun observeUpdateNotice() {
        viewModelScope.launch {
            val changelog = _viewModelState.mapNotNull { it.changelogResult.successOrNull }.first()
            val latest = changelog.pullRequests.maxOfOrNull { it.number } ?: return@launch
            val lastNotified = getLastNotifiedPrNumberUseCase().asResult().first { it !is Result.Loading }.successOrNull

            if (lastNotified != null && latest <= lastNotified) return@launch

            val newCount = lastNotified?.let { stored -> changelog.pullRequests.count { it.number > stored } }
            updateViewModelState { copy(balloons = (balloons + ProfileBalloon.SiteUpdated(newCount)).toImmutableList()) }
            saveLastNotifiedPrNumberUseCase(latest)
        }
    }

    // サーバーが GitHub に到達できなかったことは統計の欠損としてしか表れないため、最初に取得できた
    // プロフィールだけを見て一度だけ知らせる（再試行で成功した場合もその一度に含む）。
    private fun observeFallbackWarning() {
        viewModelScope.launch {
            val profile = _viewModelState.mapNotNull { it.profileResult.successOrNull }.first()
            if (profile.hasGitHubStatistics) return@launch
            updateViewModelState { copy(balloons = (balloons + ProfileBalloon.FallbackWarning).toImmutableList()) }
        }
    }

    private fun observeInteractionLog() {
        viewModelScope.launch {
            interactionLog.entries.collect { entries ->
                updateViewModelState { copy(logEntries = entries.toImmutableList()) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeProfileCode() {
        viewModelScope.launch {
            _viewModelState
                .map { it.editedProfileCode }
                .distinctUntilChanged()
                .debounce(PARSE_DEBOUNCE_MILLIS)
                .collect { code ->
                    if (code == null) {
                        updateViewModelState { copy(parsedProfile = null, profileCodeError = false) }
                    } else {
                        val parsed = parseProfileCode(code)
                        updateViewModelState {
                            if (parsed != null) {
                                copy(
                                    parsedProfile = overlayProfileAssets(parsed, profileResult.successOrNull),
                                    profileCodeError = false,
                                )
                            } else {
                                copy(profileCodeError = true)
                            }
                        }
                    }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeReadmeCode() {
        viewModelScope.launch {
            _viewModelState
                .map { it.editedReadmeCode }
                .distinctUntilChanged()
                .debounce(PARSE_DEBOUNCE_MILLIS)
                .collect { code ->
                    updateViewModelState { copy(parsedReadmeBlocks = code?.let(::parseMarkdown)) }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeWorksCode() {
        viewModelScope.launch {
            _viewModelState
                .map { it.editedWorksCode }
                .distinctUntilChanged()
                .debounce(PARSE_DEBOUNCE_MILLIS)
                .collect { code ->
                    if (code == null) {
                        updateViewModelState { copy(parsedWorks = null, worksCodeError = false) }
                    } else {
                        val parsed = parseWorksCode(code)
                        updateViewModelState {
                            if (parsed != null) {
                                copy(
                                    parsedWorks = overlayWorksAssets(parsed, worksResult.successOrNull?.items.orEmpty()),
                                    worksCodeError = false,
                                )
                            } else {
                                copy(worksCodeError = true)
                            }
                        }
                    }
                }
        }
    }

    /** `./gradlew build` の Splash 風ビルドログを遅延つきで Terminal へ流す。 */
    private fun replayBuildLog() {
        // 多重起動ガードはコルーチン起動前に同期的に立てる（起動待ちの隙間に再実行されないように）
        updateViewModelState { copy(terminalBuildRunning = true) }
        viewModelScope.launch {
            TERMINAL_BUILD_LOG_STEPS.forEach { step ->
                delay(step.delayMillis)
                updateViewModelState {
                    copy(terminalLines = (terminalLines + step.line).takeLast(TERMINAL_MAX_LINES).toImmutableList())
                }
            }
            updateViewModelState { copy(terminalBuildRunning = false) }
        }
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.UpdateLayout -> {
                if (intent.layout != _viewModelState.value.currentLayout) {
                    interactionLog.d("WindowLayout", "switch to ${intent.layout}")
                }
                // ブレークポイントを跨いで入り直したときだけ、その画面のツリー開閉状態と表示モードを
                // デフォルトへ戻す（旧実装の remember{} が破棄・再生成されるのを再現する）。
                updateViewModelState {
                    when {
                        intent.layout == currentLayout -> this
                        intent.layout == WindowLayout.Desktop -> copy(
                            desktopTreeOpen = true,
                            desktopViewMode = EditorViewMode.Split,
                            currentLayout = intent.layout,
                        )

                        else -> copy(
                            mobileTreeOpen = false,
                            mobileViewMode = EditorViewMode.PreviewOnly,
                            currentLayout = intent.layout,
                        )
                    }
                }
            }

            is ProfileIntent.UpdateSelectedPage -> {
                interactionLog.d("EditorPane", "select tab ${intent.page.fileName}")
                updateViewModelState {
                    copy(
                        selectedPage = intent.page,
                        selectedLicense = if (intent.page == selectedPage) selectedLicense else null,
                        worksSheetOpen = if (intent.page == selectedPage) worksSheetOpen else false,
                    )
                }
            }

            is ProfileIntent.UpdateSelectedPageFromTree -> {
                interactionLog.d("ProjectTree", "open ${intent.page.fileName}")
                updateViewModelState { openPage(page = intent.page, layout = intent.layout) }
            }

            is ProfileIntent.ClosePage -> {
                val pageIsOpen = intent.page in _viewModelState.value.openPages
                if (pageIsOpen) {
                    interactionLog.d("EditorPane", "close tab ${intent.page.fileName}")
                }
                updateViewModelState {
                    val closingIndex = openPages.indexOf(intent.page)
                    if (closingIndex < 0) {
                        this
                    } else {
                        val remaining = (openPages - intent.page).toImmutableList()
                        copy(
                            openPages = remaining,
                            // 実 AS と同様、選択中タブを閉じたら右隣（無ければ左隣）を選択する
                            selectedPage = when {
                                intent.page != selectedPage -> selectedPage
                                remaining.isEmpty() -> null
                                else -> remaining[minOf(closingIndex, remaining.lastIndex)]
                            },
                            selectedLicense = if (intent.page == selectedPage) null else selectedLicense,
                            worksSheetOpen = if (intent.page == selectedPage) false else worksSheetOpen,
                        )
                    }
                }
                if (pageIsOpen && _viewModelState.value.openPages.isEmpty()) {
                    interactionLog.w("EditorPane", "all tabs closed")
                }
            }

            is ProfileIntent.ToggleTree -> {
                val treeOpen = when (intent.layout) {
                    WindowLayout.Desktop -> !_viewModelState.value.desktopTreeOpen
                    WindowLayout.Mobile -> !_viewModelState.value.mobileTreeOpen
                }
                interactionLog.d("ToolWindow", if (treeOpen) "open Project" else "close Project")
                updateViewModelState {
                    when (intent.layout) {
                        WindowLayout.Desktop -> copy(desktopTreeOpen = !desktopTreeOpen)
                        WindowLayout.Mobile -> copy(mobileTreeOpen = !mobileTreeOpen)
                    }
                }
            }

            is ProfileIntent.ToggleLogcat -> {
                val logcatOpen = _viewModelState.value.openBottomTool != BottomTool.Logcat
                interactionLog.d("ToolWindow", if (logcatOpen) "open Logcat" else "close Logcat")
                updateViewModelState { toggleBottomTool(BottomTool.Logcat) }
            }

            is ProfileIntent.ToggleTodo -> {
                val todoOpen = _viewModelState.value.openBottomTool != BottomTool.Todo
                interactionLog.d("ToolWindow", if (todoOpen) "open TODO" else "close TODO")
                updateViewModelState { toggleBottomTool(BottomTool.Todo) }
            }

            is ProfileIntent.ToggleTerminal -> {
                val terminalOpen = _viewModelState.value.openBottomTool != BottomTool.Terminal
                interactionLog.d("ToolWindow", if (terminalOpen) "open Terminal" else "close Terminal")
                updateViewModelState { toggleBottomTool(BottomTool.Terminal) }
            }

            is ProfileIntent.OpenChangelog -> {
                interactionLog.d("ToolWindow", "open Git from notification")
                updateViewModelState { copy(openBottomTool = BottomTool.Changelog) }
            }

            is ProfileIntent.DismissBalloon -> {
                updateViewModelState { copy(balloons = balloons.dismiss(intent.id)) }
            }

            is ProfileIntent.ToggleChangelog -> {
                val changelogOpen = _viewModelState.value.openBottomTool != BottomTool.Changelog
                interactionLog.d("ToolWindow", if (changelogOpen) "open Git" else "close Git")
                updateViewModelState { toggleBottomTool(BottomTool.Changelog) }
            }

            is ProfileIntent.UpdateTheme -> {
                updateViewModelState { copy(isDarkTheme = intent.isDark) }
            }

            is ProfileIntent.UpdateLanguage -> {
                val previousLanguage = _viewModelState.value.language
                if (intent.language != previousLanguage) {
                    // null からの初回同期は環境の確定であって切り替えではない
                    if (previousLanguage != null) {
                        interactionLog.i("Language", "switch to ${intent.language.tag}")
                    }
                    updateViewModelState {
                        copy(
                            language = intent.language,
                            // 未編集の生成コードを新しい言語で表示し直すため、そのバッファの
                            // TextFieldState だけ作り直す（編集済みバッファは言語に依存しないので維持）
                            profileEditorResetTick = if (editedProfileCode == null) {
                                profileEditorResetTick + 1
                            } else {
                                profileEditorResetTick
                            },
                            readmeEditorResetTick = if (editedReadmeCode == null) {
                                readmeEditorResetTick + 1
                            } else {
                                readmeEditorResetTick
                            },
                            worksEditorResetTick = if (editedWorksCode == null) {
                                worksEditorResetTick + 1
                            } else {
                                worksEditorResetTick
                            },
                        )
                    }
                }
            }

            is ProfileIntent.UpdateTerminalInput -> {
                updateViewModelState { copy(terminalInput = intent.value) }
            }

            is ProfileIntent.ExecuteTerminalCommand -> {
                val input = _viewModelState.value.terminalInput.trim()
                if (input.isNotEmpty()) {
                    interactionLog.i("Terminal", "run: $input")
                }
                val echoText = if (input.isEmpty()) TERMINAL_PROMPT else "$TERMINAL_PROMPT $input"
                val echo = TerminalLine(echoText, TerminalLineKind.Command)
                val serverCommands = _viewModelState.value.terminalCommandsResult.successOrNull?.items
                    ?: persistentListOf()
                val command = parseTerminalCommand(input, serverCommands)
                val output = when (command) {
                    is TerminalCommand.Empty -> emptyList()

                    is TerminalCommand.Help -> terminalHelpLines(serverCommands).map {
                        TerminalLine(it, TerminalLineKind.Output)
                    }

                    is TerminalCommand.Text -> command.lines.map {
                        TerminalLine(it, TerminalLineKind.Output)
                    }

                    is TerminalCommand.Ls -> listOf(
                        TerminalLine(
                            EditorPage.entries.joinToString("  ") { it.fileName },
                            TerminalLineKind.Output,
                        ),
                    )

                    is TerminalCommand.Whoami -> {
                        val currentState = _viewModelState.value
                        val profile = currentState.visibleProfile
                        val language = currentState.language
                        if (profile == null || language == null) {
                            listOf(TerminalLine("whoami: profile not loaded yet", TerminalLineKind.Error))
                        } else {
                            val name = profile.name.forLanguage(language)
                            listOf(
                                TerminalLine(
                                    "${profile.handle} — $name (${profile.role}, ${profile.location})",
                                    TerminalLineKind.Output,
                                ),
                            )
                        }
                    }

                    is TerminalCommand.OpenPage -> emptyList()

                    is TerminalCommand.OpenLink -> {
                        val profile = _viewModelState.value.visibleProfile
                        when {
                            profile == null ->
                                listOf(TerminalLine("open: profile not loaded yet", TerminalLineKind.Error))

                            profile.links.none { it.type == command.service } ->
                                listOf(
                                    TerminalLine(
                                        "open: link not available: ${command.service.name.lowercase()}",
                                        TerminalLineKind.Error,
                                    ),
                                )

                            else -> emptyList()
                        }
                    }

                    is TerminalCommand.OpenInvalid -> listOf(
                        TerminalLine("open: no such target: ${command.target}", TerminalLineKind.Error),
                    )

                    is TerminalCommand.OpenUsage -> listOf(
                        TerminalLine("usage: open readme|profile|works|licenses|github|x|qiita|note", TerminalLineKind.Error),
                    )

                    is TerminalCommand.Theme -> {
                        val already = _viewModelState.value.isDarkTheme == command.isDark
                        val label = if (command.isDark) "dark" else "light"
                        if (already) {
                            listOf(TerminalLine("theme: already $label", TerminalLineKind.Output))
                        } else {
                            listOf(TerminalLine("Switched to $label theme", TerminalLineKind.Output))
                        }
                    }

                    is TerminalCommand.ThemeUsage -> listOf(
                        TerminalLine("usage: theme dark|light", TerminalLineKind.Error),
                    )

                    is TerminalCommand.Lang -> {
                        val already = _viewModelState.value.language == command.language
                        if (already) {
                            listOf(TerminalLine("lang: already ${command.language.tag}", TerminalLineKind.Output))
                        } else {
                            listOf(
                                TerminalLine(
                                    "Switched language to ${command.language.tag}",
                                    TerminalLineKind.Output,
                                ),
                            )
                        }
                    }

                    is TerminalCommand.LangUsage -> listOf(
                        TerminalLine("usage: lang en|ja", TerminalLineKind.Error),
                    )

                    is TerminalCommand.GradleBuild ->
                        if (_viewModelState.value.terminalBuildRunning) {
                            listOf(TerminalLine("build already in progress", TerminalLineKind.Error))
                        } else {
                            emptyList()
                        }

                    is TerminalCommand.Unknown -> listOf(
                        TerminalLine("zsh: command not found: ${command.name}", TerminalLineKind.Error),
                    )
                }
                updateViewModelState {
                    val appended = copy(
                        terminalInput = "",
                        terminalLines = (terminalLines + echo + output).takeLast(TERMINAL_MAX_LINES).toImmutableList(),
                    )
                    when (command) {
                        is TerminalCommand.OpenPage ->
                            appended.openPage(page = command.page, layout = currentLayout ?: WindowLayout.Desktop)

                        is TerminalCommand.OpenLink -> {
                            val url = visibleProfile?.links?.firstOrNull { it.type == command.service }?.url
                            if (url == null) appended else appended.copy(effect = ProfileEffect.OpenUrl(url))
                        }

                        is TerminalCommand.Theme ->
                            if (isDarkTheme == command.isDark) {
                                appended
                            } else {
                                appended.copy(effect = ProfileEffect.SwitchTheme(command.isDark))
                            }

                        is TerminalCommand.Lang ->
                            if (language == command.language) {
                                appended
                            } else {
                                appended.copy(effect = ProfileEffect.SwitchLanguage(command.language))
                            }

                        else -> appended
                    }
                }
                if (command is TerminalCommand.GradleBuild && !_viewModelState.value.terminalBuildRunning) {
                    replayBuildLog()
                }
            }

            is ProfileIntent.ClearLogcat -> {
                interactionLog.clear()
            }

            is ProfileIntent.UpdateLogcatPanelHeight -> {
                updateViewModelState { copy(logcatPanelHeight = intent.height) }
            }

            is ProfileIntent.UpdateTodoPanelHeight -> {
                updateViewModelState { copy(todoPanelHeight = intent.height) }
            }

            is ProfileIntent.UpdateTerminalPanelHeight -> {
                updateViewModelState { copy(terminalPanelHeight = intent.height) }
            }

            is ProfileIntent.UpdateChangelogPanelHeight -> {
                updateViewModelState { copy(changelogPanelHeight = intent.height) }
            }

            is ProfileIntent.UpdateViewMode -> {
                interactionLog.d("EditorPane", "view mode ${intent.viewMode}")
                updateViewModelState {
                    when (intent.layout) {
                        WindowLayout.Desktop -> copy(desktopViewMode = intent.viewMode)
                        WindowLayout.Mobile -> copy(mobileViewMode = intent.viewMode)
                    }
                }
            }

            is ProfileIntent.UpdateProfileCode -> {
                updateViewModelState { copy(editedProfileCode = intent.code) }
            }

            is ProfileIntent.UpdateReadmeCode -> {
                updateViewModelState { copy(editedReadmeCode = intent.code) }
            }

            is ProfileIntent.UpdateWorksCode -> {
                updateViewModelState { copy(editedWorksCode = intent.code) }
            }

            is ProfileIntent.ResetEditorCode -> {
                updateViewModelState {
                    copy(
                        editedProfileCode = null,
                        parsedProfile = null,
                        profileCodeError = false,
                        editedReadmeCode = null,
                        parsedReadmeBlocks = null,
                        editedWorksCode = null,
                        parsedWorks = null,
                        worksCodeError = false,
                        profileEditorResetTick = profileEditorResetTick + 1,
                        readmeEditorResetTick = readmeEditorResetTick + 1,
                        worksEditorResetTick = worksEditorResetTick + 1,
                    )
                }
            }

            is ProfileIntent.OpenUrl -> {
                interactionLog.i("Link", "open ${intent.url}")
                updateViewModelState { copy(effect = ProfileEffect.OpenUrl(intent.url)) }
            }

            is ProfileIntent.OpenPage -> {
                interactionLog.d("ProjectTree", "open ${intent.page.fileName}")
                updateViewModelState { openPage(page = intent.page, layout = currentLayout ?: WindowLayout.Desktop) }
            }

            is ProfileIntent.OpenSearchEverywhere -> {
                updateViewModelState { copy(effect = ProfileEffect.NavigateSearchEverywhere) }
            }

            is ProfileIntent.RetryBackendData -> {
                interactionLog.i("Preview", "retry backend data fetch")
                // 失敗したストリームだけ取り直す。成功済み側まで再収集すると asResult() の onStart が
                // Loading を再送出し、表示済みの editor / preview がスケルトンへ巻き戻ってしまう。
                // 再試行中は Error でなく Loading になるため、連打しても収集は多重化しない。
                // SingleFlightCache は失敗をキャッシュしないため再収集で fetch が走る。
                if (_viewModelState.value.profileResult is Result.Error) loadProfile()
                if (_viewModelState.value.contributionsResult is Result.Error) loadContributions()
                if (_viewModelState.value.issuesResult is Result.Error) loadIssues()
                if (_viewModelState.value.worksResult is Result.Error) loadWorks()
                if (_viewModelState.value.readmeResult is Result.Error) loadReadme()
                if (_viewModelState.value.terminalCommandsResult is Result.Error) loadTerminalCommands()
                if (_viewModelState.value.changelogResult is Result.Error) loadChangelog()
            }

            is ProfileIntent.UpdateSelectedLicense -> {
                if (intent.license != null) {
                    interactionLog.d("LicenseSheet", "open ${intent.license.name}")
                } else {
                    interactionLog.d("LicenseSheet", "close")
                }
                updateViewModelState { copy(selectedLicense = intent.license) }
            }

            is ProfileIntent.UpdateWorksSheetVisibility -> {
                updateViewModelState { copy(worksSheetOpen = intent.isVisible) }
            }

            is ProfileIntent.ConsumeEffect -> {
                updateViewModelState { copy(effect = null) }
            }
        }
    }
}

/** Preview / whoami が見せるプロフィール。 */
private val ProfileViewModelState.visibleProfile: Profile?
    get() = parsedProfile ?: profileResult.successOrNull

private fun ImmutableList<ProfileBalloon>.dismiss(id: String): ImmutableList<ProfileBalloon> =
    filterNot { it.id == id }.toImmutableList()

// 実 AS の下部ドックと同様、開くときは他の下部ツールウィンドウを閉じる（スロットは1つ）。
private fun ProfileViewModelState.toggleBottomTool(tool: BottomTool): ProfileViewModelState =
    copy(openBottomTool = if (openBottomTool == tool) null else tool)

private fun ProfileViewModelState.openPage(page: EditorPage, layout: WindowLayout): ProfileViewModelState = copy(
    selectedPage = page,
    // 実 IDE と同様、ツリーから開いたファイルだけがタブに追加される
    openPages = if (page in openPages) {
        openPages
    } else {
        (openPages + page).toImmutableList()
    },
    selectedLicense = if (page == selectedPage) selectedLicense else null,
    worksSheetOpen = if (page == selectedPage) worksSheetOpen else false,
    mobileTreeOpen = if (layout == WindowLayout.Mobile) false else mobileTreeOpen,
)

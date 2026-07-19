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
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.domain.usecase.GetContributionsUseCase
import io.github.kei_1111.app.core.domain.usecase.GetLicensesUseCase
import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.app.core.mvi.MviViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
internal class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val getContributionsUseCase: GetContributionsUseCase,
    private val getLicensesUseCase: GetLicensesUseCase,
) : MviViewModel<ProfileViewModelState, ProfileState, ProfileIntent>() {

    override fun createInitialViewModelState() = ProfileViewModelState()
    override fun createInitialState() = ProfileState()

    init {
        loadProfile()
        loadContributions()
        loadLicenses()
        observeInteractionLog()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            getProfileUseCase().asResult().collect { result ->
                updateViewModelState { copy(profileResult = result) }
            }
        }
    }

    private fun loadLicenses() {
        viewModelScope.launch {
            getLicensesUseCase().asResult().collect { result ->
                if (result is Result.Error) {
                    InteractionLog.e("LicensesRepository", "failed to load third-party licenses")
                }
                updateViewModelState { copy(licensesResult = result) }
            }
        }
    }

    private fun loadContributions() {
        viewModelScope.launch {
            getContributionsUseCase().asResult().collect { result ->
                updateViewModelState { copy(contributionsResult = result) }
            }
        }
    }

    private fun observeInteractionLog() {
        viewModelScope.launch {
            InteractionLog.entries.collect { entries ->
                updateViewModelState { copy(logEntries = entries.toImmutableList()) }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.UpdateLayout -> {
                if (intent.layout != _viewModelState.value.currentLayout) {
                    InteractionLog.i("WindowLayout", intent.layout.toString())
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
                InteractionLog.d("EditorPane", "select tab ${intent.page.fileName}")
                updateViewModelState {
                    copy(
                        selectedPage = intent.page,
                        // 別ページへ移るときは開いていたライセンスシートを閉じる（同一ページの再選択では維持）
                        selectedLicense = if (intent.page == selectedPage) selectedLicense else null,
                    )
                }
            }

            is ProfileIntent.UpdateSelectedPageFromTree -> {
                InteractionLog.d("ProjectTree", "open ${intent.page.fileName}")
                updateViewModelState {
                    copy(
                        selectedPage = intent.page,
                        // 実 IDE と同様、ツリーから開いたファイルだけがタブに追加される
                        openPages = if (intent.page in openPages) {
                            openPages
                        } else {
                            (openPages + intent.page).toImmutableList()
                        },
                        selectedLicense = if (intent.page == selectedPage) selectedLicense else null,
                        mobileTreeOpen = if (intent.layout == WindowLayout.Mobile) false else mobileTreeOpen,
                    )
                }
            }

            is ProfileIntent.ClosePage -> {
                val pageIsOpen = intent.page in _viewModelState.value.openPages
                if (pageIsOpen) {
                    InteractionLog.d("EditorPane", "close tab ${intent.page.fileName}")
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
                        )
                    }
                }
                if (pageIsOpen && _viewModelState.value.openPages.isEmpty()) {
                    InteractionLog.w("EditorPane", "all tabs closed")
                }
            }

            is ProfileIntent.ToggleTree -> {
                val treeOpen = when (intent.layout) {
                    WindowLayout.Desktop -> !_viewModelState.value.desktopTreeOpen
                    WindowLayout.Mobile -> !_viewModelState.value.mobileTreeOpen
                }
                InteractionLog.d("ToolWindow", if (treeOpen) "open Project" else "close Project")
                updateViewModelState {
                    when (intent.layout) {
                        WindowLayout.Desktop -> copy(desktopTreeOpen = !desktopTreeOpen)
                        WindowLayout.Mobile -> copy(mobileTreeOpen = !mobileTreeOpen)
                    }
                }
            }

            is ProfileIntent.ToggleLogcat -> {
                val logcatOpen = !_viewModelState.value.logcatOpen
                InteractionLog.d("ToolWindow", if (logcatOpen) "open Logcat" else "close Logcat")
                updateViewModelState { copy(logcatOpen = !this.logcatOpen) }
            }

            is ProfileIntent.ClearLogcat -> {
                InteractionLog.clear()
            }

            is ProfileIntent.UpdateLogcatPanelHeight -> {
                updateViewModelState { copy(logcatPanelHeight = intent.height) }
            }

            is ProfileIntent.UpdateViewMode -> {
                InteractionLog.d("EditorPane", "view mode ${intent.viewMode}")
                updateViewModelState {
                    when (intent.layout) {
                        WindowLayout.Desktop -> copy(desktopViewMode = intent.viewMode)
                        WindowLayout.Mobile -> copy(mobileViewMode = intent.viewMode)
                    }
                }
            }

            is ProfileIntent.OpenUrl -> {
                InteractionLog.i("OpenUrl", intent.url)
                updateViewModelState { copy(effect = ProfileEffect.OpenUrl(intent.url)) }
            }

            is ProfileIntent.UpdateSelectedLicense -> {
                if (intent.license != null) {
                    InteractionLog.d("LicenseSheet", "open ${intent.license.name}")
                } else {
                    InteractionLog.d("LicenseSheet", "close")
                }
                updateViewModelState { copy(selectedLicense = intent.license) }
            }

            is ProfileIntent.ConsumeEffect -> {
                updateViewModelState { copy(effect = null) }
            }
        }
    }
}

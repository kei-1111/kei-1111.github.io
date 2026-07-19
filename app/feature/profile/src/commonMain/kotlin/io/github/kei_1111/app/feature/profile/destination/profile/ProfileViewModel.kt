package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.kei_1111.app.core.common.result.asResult
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.domain.usecase.GetContributionsUseCase
import io.github.kei_1111.app.core.domain.usecase.GetLicensesUseCase
import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.app.core.mvi.MviViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val PARSE_DEBOUNCE_MILLIS = 300L

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
        observeProfileCode()
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
                                copy(parsedProfile = parsed, profileCodeError = false)
                            } else {
                                copy(profileCodeError = true)
                            }
                        }
                    }
                }
        }
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.UpdateLayout -> {
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
                updateViewModelState {
                    copy(
                        selectedPage = intent.page,
                        // 別ページへ移るときは開いていたライセンスシートを閉じる（同一ページの再選択では維持）
                        selectedLicense = if (intent.page == selectedPage) selectedLicense else null,
                    )
                }
            }

            is ProfileIntent.UpdateSelectedPageFromTree -> {
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
            }

            is ProfileIntent.ToggleTree -> {
                updateViewModelState {
                    when (intent.layout) {
                        WindowLayout.Desktop -> copy(desktopTreeOpen = !desktopTreeOpen)
                        WindowLayout.Mobile -> copy(mobileTreeOpen = !mobileTreeOpen)
                    }
                }
            }

            is ProfileIntent.UpdateViewMode -> {
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

            is ProfileIntent.ResetProfileCode -> {
                updateViewModelState {
                    copy(
                        editedProfileCode = null,
                        parsedProfile = null,
                        profileCodeError = false,
                        editorResetTick = editorResetTick + 1,
                    )
                }
            }

            is ProfileIntent.OpenUrl -> {
                updateViewModelState { copy(effect = ProfileEffect.OpenUrl(intent.url)) }
            }

            is ProfileIntent.UpdateSelectedLicense -> {
                updateViewModelState { copy(selectedLicense = intent.license) }
            }

            is ProfileIntent.ConsumeEffect -> {
                updateViewModelState { copy(effect = null) }
            }
        }
    }
}

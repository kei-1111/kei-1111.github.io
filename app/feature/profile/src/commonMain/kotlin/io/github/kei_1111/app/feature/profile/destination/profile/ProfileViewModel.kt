package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.common.result.asResult
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.domain.usecase.GetContributionsUseCase
import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.app.core.mvi.MviViewModel
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
internal class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val getContributionsUseCase: GetContributionsUseCase,
) : MviViewModel<ProfileViewModelState, ProfileState, ProfileIntent>() {

    // getProfileUseCase() currently emits exactly once (flowOf), but guard against a future
    // multi-emission profile source starting the contributions fetch more than once.
    private var contributionsLoadStarted = false

    override fun createInitialViewModelState() = ProfileViewModelState()
    override fun createInitialState() = ProfileState()

    init {
        viewModelScope.launch {
            getProfileUseCase().asResult().collect { result ->
                when (result) {
                    is Result.Success -> {
                        updateViewModelState { copy(profileResult = result) }
                        if (!contributionsLoadStarted) {
                            contributionsLoadStarted = true
                            loadContributions(result.data.handle)
                        }
                    }

                    is Result.Loading -> updateViewModelState { copy(profileResult = result) }
                    is Result.Error -> updateViewModelState { copy(profileResult = result) }
                }
            }
        }
    }

    private fun loadContributions(handle: String) {
        viewModelScope.launch {
            getContributionsUseCase(handle).asResult().collect { result ->
                updateViewModelState { copy(contributionsResult = result) }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
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
                updateViewModelState { copy(selectedPage = intent.page) }
            }

            is ProfileIntent.UpdateSelectedPageFromTree -> {
                updateViewModelState {
                    copy(
                        selectedPage = intent.page,
                        mobileTreeOpen = if (intent.layout == WindowLayout.Mobile) false else mobileTreeOpen,
                    )
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

            is ProfileIntent.OpenUrl -> {
                updateViewModelState { copy(effect = ProfileEffect.OpenUrl(intent.url)) }
            }

            is ProfileIntent.ConsumeEffect -> {
                updateViewModelState { copy(effect = null) }
            }
        }
    }
}

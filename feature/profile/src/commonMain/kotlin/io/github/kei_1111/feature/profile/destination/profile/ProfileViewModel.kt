package io.github.kei_1111.feature.profile.destination.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.kei_1111.core.common.result.asResult
import io.github.kei_1111.core.domain.usecase.GetContributionsUseCase
import io.github.kei_1111.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.core.mvi.MviViewModel
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
            getProfileUseCase().collect { profile ->
                updateViewModelState { copy(profile = profile) }
                if (!contributionsLoadStarted) {
                    contributionsLoadStarted = true
                    loadContributions(profile.handle)
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

    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.OnLayoutChanged -> onLayoutChanged(intent.layout)

            is ProfileIntent.OnEditorTabClick -> {
                updateViewModelState { copy(selectedPage = intent.page) }
            }

            is ProfileIntent.OnTreeRowClick -> {
                updateViewModelState {
                    copy(
                        selectedPage = intent.page,
                        mobileTreeOpen = if (intent.layout == ProfileLayout.Mobile) false else mobileTreeOpen,
                    )
                }
            }

            is ProfileIntent.OnToggleTreeClick -> {
                updateViewModelState {
                    when (intent.layout) {
                        ProfileLayout.Desktop -> copy(desktopTreeOpen = !desktopTreeOpen)
                        ProfileLayout.Mobile -> copy(mobileTreeOpen = !mobileTreeOpen)
                    }
                }
            }

            is ProfileIntent.OnViewModeSelect -> {
                updateViewModelState {
                    when (intent.layout) {
                        ProfileLayout.Desktop -> copy(desktopViewMode = intent.viewMode)
                        ProfileLayout.Mobile -> copy(mobileViewMode = intent.viewMode)
                    }
                }
            }

            is ProfileIntent.OnUrlClick -> {
                updateViewModelState { copy(effect = ProfileEffect.OpenUrl(intent.url)) }
            }

            is ProfileIntent.ConsumeEffect -> {
                updateViewModelState { copy(effect = null) }
            }
        }
    }

    /**
     * ブレークポイントを跨いで [layout] に入り直したときのみ、その画面のツリー開閉状態と
     * 表示モードをデフォルトへ戻す（旧実装の remember{} が破棄・再生成されるのを再現する）。
     */
    private fun onLayoutChanged(layout: ProfileLayout) {
        updateViewModelState {
            if (layout == currentLayout) {
                this
            } else {
                when (layout) {
                    ProfileLayout.Desktop -> copy(
                        desktopTreeOpen = true,
                        desktopViewMode = EditorViewMode.Split,
                        currentLayout = layout,
                    )

                    ProfileLayout.Mobile -> copy(
                        mobileTreeOpen = false,
                        mobileViewMode = EditorViewMode.PreviewOnly,
                        currentLayout = layout,
                    )
                }
            }
        }
    }
}

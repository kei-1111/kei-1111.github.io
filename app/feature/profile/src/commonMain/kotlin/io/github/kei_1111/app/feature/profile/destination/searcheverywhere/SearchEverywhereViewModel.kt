package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.kei_1111.app.core.common.logging.InteractionLog
import io.github.kei_1111.app.core.common.result.asResult
import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.app.core.mvi.MviViewModel
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.toEffect
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
internal class SearchEverywhereViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val interactionLog: InteractionLog,
) : MviViewModel<SearchEverywhereViewModelState, SearchEverywhereState, SearchEverywhereIntent>() {

    override fun createInitialViewModelState() = SearchEverywhereViewModelState()
    override fun createInitialState() = SearchEverywhereState()

    init {
        interactionLog.d("SearchEverywhere", "open")
        loadProfile()
    }

    // Dialog エントリ破棄時に必ず呼ばれるため、Esc・外側クリック・エントリ実行のどの閉じ方でも 1 回だけ記録される
    override fun onCleared() {
        interactionLog.d("SearchEverywhere", "close")
    }

    private fun loadProfile() {
        viewModelScope.launch {
            getProfileUseCase().asResult().collect { result ->
                updateViewModelState { copy(profileResult = result) }
            }
        }
    }

    override fun onIntent(intent: SearchEverywhereIntent) {
        when (intent) {
            is SearchEverywhereIntent.UpdateQuery -> {
                updateViewModelState { copy(query = intent.query, selectedIndex = 0) }
            }

            is SearchEverywhereIntent.UpdateSelectedTab -> {
                updateViewModelState { copy(selectedTab = intent.tab, selectedIndex = 0) }
            }

            is SearchEverywhereIntent.CycleTab -> {
                updateViewModelState {
                    val tabs = SearchEverywhereTab.entries
                    val nextIndex = (tabs.indexOf(selectedTab) + intent.delta + tabs.size) % tabs.size
                    copy(selectedTab = tabs[nextIndex], selectedIndex = 0)
                }
            }

            is SearchEverywhereIntent.MoveSelection -> {
                updateViewModelState {
                    copy(selectedIndex = clampToResults(selectedIndex + intent.delta, results()))
                }
            }

            is SearchEverywhereIntent.OpenEntry -> {
                interactionLog.i("SearchEverywhere", "execute ${intent.entry.categoryLabel} ${intent.entry.name}")
                updateViewModelState { copy(effect = intent.entry.toEffect()) }
            }

            SearchEverywhereIntent.OpenSelectedEntry -> {
                _viewModelState.value.selectedEntry()?.let { entry ->
                    interactionLog.i("SearchEverywhere", "execute ${entry.categoryLabel} ${entry.name}")
                    updateViewModelState { copy(effect = entry.toEffect()) }
                }
            }

            SearchEverywhereIntent.ConsumeEffect -> {
                updateViewModelState { copy(effect = null) }
            }
        }
    }
}

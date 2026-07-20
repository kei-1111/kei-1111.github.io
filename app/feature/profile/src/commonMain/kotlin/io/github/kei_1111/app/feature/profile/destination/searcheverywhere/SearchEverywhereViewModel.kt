package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.kei_1111.app.core.common.result.asResult
import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.app.core.mvi.MviViewModel
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
internal class SearchEverywhereViewModel(
    private val getProfileUseCase: GetProfileUseCase,
) : MviViewModel<SearchEverywhereViewModelState, SearchEverywhereState, SearchEverywhereIntent>() {

    override fun createInitialViewModelState() = SearchEverywhereViewModelState()
    override fun createInitialState() = SearchEverywhereState()

    init {
        loadProfile()
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
                updateViewModelState { copy(effect = effectFor(intent.entry)) }
            }

            SearchEverywhereIntent.OpenSelectedEntry -> {
                updateViewModelState { selectedEntry()?.let { copy(effect = effectFor(it)) } ?: this }
            }

            SearchEverywhereIntent.Dismiss -> {
                updateViewModelState { copy(effect = SearchEverywhereEffect.NavigateBack) }
            }

            SearchEverywhereIntent.ConsumeEffect -> {
                updateViewModelState { copy(effect = null) }
            }
        }
    }
}

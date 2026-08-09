package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.kei_1111.app.core.common.logging.InteractionLog
import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.app.core.mvi.MviViewModel
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereTab
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.toEffect

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

    private fun loadProfile() = getProfileUseCase().collectAsResult { copy(profileResult = it) }

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
                    copy(selectedIndex = clampToResults(selectedIndex + intent.delta, searchResults()))
                }
            }

            is SearchEverywhereIntent.OpenEntry -> {
                interactionLog.i("SearchEverywhere", "execute ${intent.entry.categoryLabel} ${intent.entry.name}")
                updateViewModelState { copy(effect = intent.entry.toEffect()) }
            }

            is SearchEverywhereIntent.OpenSelectedEntry -> {
                // 画面がハイライトしている行と同じ導出で開く対象を決め、Enter と表示を一致させる
                val current = _viewModelState.value
                val results = current.searchResults()
                results.getOrNull(current.clampToResults(current.selectedIndex, results))?.let { entry ->
                    interactionLog.i("SearchEverywhere", "execute ${entry.categoryLabel} ${entry.name}")
                    updateViewModelState { copy(effect = entry.toEffect()) }
                }
            }

            is SearchEverywhereIntent.ConsumeEffect -> {
                updateViewModelState { copy(effect = null) }
            }
        }
    }
}

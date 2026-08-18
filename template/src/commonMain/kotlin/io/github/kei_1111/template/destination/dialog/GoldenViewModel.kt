package io.github.kei_1111.template.destination.dialog

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.kei_1111.app.core.mvi.MviViewModel

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
internal class GoldenViewModel(
    // PLACEHOLDER: UseCases from core:domain only — never a Repository
) : MviViewModel<GoldenViewModelState, GoldenState, GoldenIntent, GoldenEffect>() {

    override fun createInitialViewModelState() = GoldenViewModelState()
    override fun applyEffect(state: GoldenState, effect: GoldenEffect?) = state.copy(effect = effect)
    override fun clearEffect(viewModelState: GoldenViewModelState) = viewModelState.copy(effect = null)

    override fun onIntent(intent: GoldenIntent) {
        when (intent) {
            // PLACEHOLDER: this destination's intent branches, written inline in the when

            is GoldenIntent.ConsumeEffect -> consumeEffect()
        }
    }
}

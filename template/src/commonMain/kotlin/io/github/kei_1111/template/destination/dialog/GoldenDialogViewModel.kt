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
internal class GoldenDialogViewModel(
    // PLACEHOLDER: UseCases from core:domain only — never a Repository
) : MviViewModel<GoldenDialogViewModelState, GoldenDialogState, GoldenDialogIntent, GoldenDialogEffect>() {

    override fun createInitialViewModelState() = GoldenDialogViewModelState()
    override fun applyEffect(state: GoldenDialogState, effect: GoldenDialogEffect?) = state.copy(effect = effect)
    override fun clearEffect(viewModelState: GoldenDialogViewModelState) = viewModelState.copy(effect = null)

    override fun onIntent(intent: GoldenDialogIntent) {
        when (intent) {
            // PLACEHOLDER: this destination's intent branches, written inline in the when

            is GoldenDialogIntent.ConsumeEffect -> consumeEffect()
        }
    }
}

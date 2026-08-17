package io.github.kei_1111.template.dialog.destination.golden

import io.github.kei_1111.app.core.mvi.ViewModelState

internal data class GoldenViewModelState(
    // PLACEHOLDER: ViewModel-internal fields, e.g. raw Result<T> values from UseCases
    override val effect: GoldenEffect? = null,
) : ViewModelState<GoldenState, GoldenEffect> {
    override fun toState() = GoldenState(
        // PLACEHOLDER: derive UI-facing values — unwrap results via xxxResult.successOrNull
    )
}

package io.github.kei_1111.template.destination.dialog

import io.github.kei_1111.app.core.mvi.ViewModelState

internal data class GoldenDialogViewModelState(
    // PLACEHOLDER: ViewModel-internal fields, e.g. raw Result<T> values from UseCases
    override val effect: GoldenDialogEffect? = null,
) : ViewModelState<GoldenDialogState, GoldenDialogEffect> {
    override fun toState() = GoldenDialogState(
        // PLACEHOLDER: derive UI-facing values — unwrap results via xxxResult.successOrNull
    )
}

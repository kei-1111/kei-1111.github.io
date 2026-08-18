package io.github.kei_1111.template.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kei_1111.app.core.navigation.dialogTransition
import io.github.kei_1111.template.destination.dialog.GoldenDialogDialogRoot
import io.github.kei_1111.template.destination.dialog.GoldenDialogViewModel

fun EntryProviderScope<NavKey>.templateDialogEntries() {
    entry<GoldenDialog>(metadata = dialogTransition()) {
        val viewModel: GoldenDialogViewModel = metroViewModel()
        // PLACEHOLDER: add result reception here only when confirmed in Prerequisites #7
        GoldenDialogDialogRoot(viewModel = viewModel)
    }
}

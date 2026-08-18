package io.github.kei_1111.template.navigation.dialog

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kei_1111.app.core.navigation.dialogTransition
import io.github.kei_1111.template.destination.dialog.GoldenDialogRoot
import io.github.kei_1111.template.destination.dialog.GoldenViewModel

fun EntryProviderScope<NavKey>.templateEntries() {
    entry<Golden>(metadata = dialogTransition()) {
        val viewModel: GoldenViewModel = metroViewModel()
        // PLACEHOLDER: add result reception here only when confirmed in Prerequisites #7
        GoldenDialogRoot(viewModel = viewModel)
    }
}

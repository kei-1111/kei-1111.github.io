package io.github.kei_1111.template.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kei_1111.template.destination.screen.GoldenScreenRoot
import io.github.kei_1111.template.destination.screen.GoldenViewModel

fun EntryProviderScope<NavKey>.templateScreenEntries() {
    entry<Golden> {
        val viewModel: GoldenViewModel = metroViewModel()
        // PLACEHOLDER: add result reception here only when confirmed in Prerequisites #7
        GoldenScreenRoot(viewModel = viewModel)
    }
}

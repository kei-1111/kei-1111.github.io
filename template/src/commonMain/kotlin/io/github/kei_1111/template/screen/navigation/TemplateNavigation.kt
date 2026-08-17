package io.github.kei_1111.template.screen.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kei_1111.template.screen.destination.golden.GoldenScreenRoot
import io.github.kei_1111.template.screen.destination.golden.GoldenViewModel

fun EntryProviderScope<NavKey>.templateEntries() {
    entry<Golden> {
        val viewModel: GoldenViewModel = metroViewModel()
        // PLACEHOLDER: add result reception here only when confirmed in Prerequisites #7
        GoldenScreenRoot(viewModel = viewModel)
    }
}

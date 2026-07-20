package io.github.kei_1111.app.feature.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kei_1111.app.core.navigation.LocalResultEventBus
import io.github.kei_1111.app.core.navigation.ResultEffect
import io.github.kei_1111.app.core.navigation.dialogTransition
import io.github.kei_1111.app.core.navigation.scrimlessDialogProperties
import io.github.kei_1111.app.feature.profile.destination.profile.ProfileIntent
import io.github.kei_1111.app.feature.profile.destination.profile.ProfileScreenRoot
import io.github.kei_1111.app.feature.profile.destination.profile.ProfileViewModel
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.SearchEverywhereDialogRoot
import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.SearchEverywhereViewModel

fun EntryProviderScope<NavKey>.profileEntries(
    navigateSearchEverywhere: () -> Unit,
    navigateBack: () -> Unit,
) {
    entry<Profile> {
        val viewModel: ProfileViewModel = metroViewModel()
        val resultEventBus = LocalResultEventBus.current

        ResultEffect<SearchEverywhereResult>(resultEventBus) { result ->
            viewModel.onIntent(ProfileIntent.OpenPage(result.page))
        }

        ProfileScreenRoot(
            viewModel = viewModel,
            navigateSearchEverywhere = navigateSearchEverywhere,
        )
    }
    entry<SearchEverywhere>(
        // 実 AS の Search Everywhere は背後の IDE を暗転させない。
        metadata = dialogTransition(scrimlessDialogProperties()),
    ) {
        val viewModel: SearchEverywhereViewModel = metroViewModel()
        SearchEverywhereDialogRoot(
            viewModel = viewModel,
            navigateBack = navigateBack,
        )
    }
}

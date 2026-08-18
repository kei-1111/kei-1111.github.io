package io.github.kei_1111.app.core.testing

import io.github.kei_1111.app.core.mvi.Intent
import io.github.kei_1111.app.core.mvi.MviViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent

@OptIn(ExperimentalCoroutinesApi::class)
fun <I : Intent> TestScope.dispatch(viewModel: MviViewModel<*, *, I, *>, intent: I) {
    viewModel.onIntent(intent)
    runCurrent()
}

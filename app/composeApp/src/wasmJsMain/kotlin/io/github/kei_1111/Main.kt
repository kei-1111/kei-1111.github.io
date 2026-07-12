package io.github.kei_1111

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.zacsweers.metro.createGraph
import io.github.kei_1111.di.AppGraph
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appGraph = createGraph<AppGraph>()

    ComposeViewport(document.body!!) {
        App(appGraph = appGraph)
    }
}

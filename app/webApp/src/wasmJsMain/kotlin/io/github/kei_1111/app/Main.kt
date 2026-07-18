package io.github.kei_1111.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.zacsweers.metro.createGraph
import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.core.designsystem.language.KeiLanguageController
import io.github.kei_1111.app.core.utils.browserLanguageTag
import io.github.kei_1111.app.di.AppGraph
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appGraph = createGraph<AppGraph>()
    KeiLanguageController.initialize(KeiLanguage.fromTag(browserLanguageTag()))

    ComposeViewport(document.body!!) {
        App(appGraph = appGraph)
    }
}

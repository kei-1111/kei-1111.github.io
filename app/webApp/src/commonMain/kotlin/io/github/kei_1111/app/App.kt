package io.github.kei_1111.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import io.github.kei_1111.app.core.designsystem.language.KeiLanguageController
import io.github.kei_1111.app.core.designsystem.language.KeiLanguageResourceEnvironment
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.utils.setDocumentLanguage
import io.github.kei_1111.app.di.AppGraph
import io.github.kei_1111.app.navigation.AppNavDisplay

@Suppress("ModifierMissing")
@Composable
fun App(appGraph: AppGraph) {
    val language = KeiLanguageController.language
    LaunchedEffect(language) { setDocumentLanguage(language.tag) }

    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
    ) {
        KeiLanguageResourceEnvironment {
            KeiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KeiTheme.colors.desk,
                ) {
                    AppNavDisplay()
                }
            }
        }
    }
}

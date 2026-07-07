package io.github.kei_1111

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import io.github.kei_1111.core.designsystem.theme.KeiTheme
import io.github.kei_1111.core.designsystem.theme.keiColorScheme
import io.github.kei_1111.di.AppGraph
import io.github.kei_1111.navigation.AppNavDisplay

@Suppress("ModifierMissing")
@Composable
fun App(appGraph: AppGraph) {
    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
    ) {
        KeiTheme(colorScheme = keiColorScheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = KeiTheme.colors.desk,
            ) {
                AppNavDisplay()
            }
        }
    }
}

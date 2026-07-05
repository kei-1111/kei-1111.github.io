package io.github.kei_1111

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import io.github.kei_1111.core.designsystem.theme.AppTheme
import io.github.kei_1111.core.designsystem.theme.IdeColors
import io.github.kei_1111.di.AppGraph
import io.github.kei_1111.navigation.NavGraph

@Suppress("ModifierMissing")
@Composable
fun App(appGraph: AppGraph) {
    val navController = rememberNavController()

    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
    ) {
        AppTheme(
            darkTheme = true,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = IdeColors.Desk,
            ) {
                NavGraph(
                    navController = navController,
                )
            }
        }
    }
}

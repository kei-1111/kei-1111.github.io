package io.github.kei_1111.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.di.AppGraph
import io.github.kei_1111.app.navigation.AppNavDisplay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.drop

@Suppress("ModifierMissing")
@Composable
fun App(
    appGraph: AppGraph,
    initialIsDark: Boolean,
) {
    // テーマ状態の唯一の所有者。変更能力は onToggleTheme のコールバック配線でのみ配布する
    var isDark by remember(initialIsDark) { mutableStateOf(initialIsDark) }
    val onToggleTheme = remember { { isDark = !isDark } }

    LaunchedEffect(appGraph) {
        snapshotFlow { isDark }
            .drop(1) // 初回 emission は復元値そのものなので保存しない
            .collect { value ->
                try {
                    appGraph.themeRepository.saveIsDark(value)
                } catch (_: Exception) {
                    // 保存は best-effort: 失敗（quota 超過など）でも監視は続け、次回の切り替えで再度保存する
                    currentCoroutineContext().ensureActive()
                }
            }
    }

    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
    ) {
        KeiTheme(isDark = isDark) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = KeiTheme.colors.desk,
            ) {
                AppNavDisplay(onToggleTheme = onToggleTheme)
            }
        }
    }
}

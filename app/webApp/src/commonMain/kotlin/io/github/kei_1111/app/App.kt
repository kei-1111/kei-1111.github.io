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
import io.github.kei_1111.app.core.common.coroutines.runBestEffort
import io.github.kei_1111.app.core.designsystem.language.KeiLanguageController
import io.github.kei_1111.app.core.designsystem.language.KeiLanguageResourceEnvironment
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.utils.setDocumentLanguage
import io.github.kei_1111.app.di.AppGraph
import io.github.kei_1111.app.navigation.AppNavDisplay
import kotlinx.coroutines.flow.drop

@Suppress("ModifierMissing")
@Composable
fun App(
    appGraph: AppGraph,
    initialIsDark: Boolean,
) {
    val language = KeiLanguageController.language
    LaunchedEffect(language) { setDocumentLanguage(language.tag) }

    // テーマ状態の唯一の所有者。変更能力は onToggleTheme のコールバック配線でのみ配布する
    var isDark by remember(initialIsDark) { mutableStateOf(initialIsDark) }
    val onToggleTheme = remember { { isDark = !isDark } }
    // 言語状態は KeiLanguageController が所有する。変更能力はテーマと同様コールバック配線でのみ配布する
    val onToggleLanguage = remember { { KeiLanguageController.toggle() } }

    LaunchedEffect(appGraph) {
        snapshotFlow { isDark }
            .drop(1) // 初回 emission は復元値そのものなので保存しない
            .collect { value ->
                appGraph.interactionLog.d("Theme", "save isDark=$value")
                // 保存は best-effort: 失敗（quota 超過など）でも監視は続け、次回の切り替えで再度保存する
                runBestEffort { appGraph.themeRepository.saveIsDark(value) }
            }
    }

    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
    ) {
        KeiLanguageResourceEnvironment(isDark = isDark) {
            KeiTheme(isDark = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KeiTheme.colors.desk,
                ) {
                    AppNavDisplay(
                        onToggleTheme = onToggleTheme,
                        onToggleLanguage = onToggleLanguage,
                        interactionLog = appGraph.interactionLog,
                        navKeySerializers = appGraph.navKeySerializers,
                    )
                }
            }
        }
    }
}

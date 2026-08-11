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
import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.core.designsystem.language.KeiLanguageResourceEnvironment
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.utils.setDocumentLanguage
import io.github.kei_1111.app.di.AppGraph
import io.github.kei_1111.app.navigation.AppNavDisplay
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.LocalResourceReader

@OptIn(ExperimentalResourceApi::class)
@Suppress("ModifierMissing")
@Composable
fun App(
    appGraph: AppGraph,
    initialIsDark: Boolean,
    initialLanguage: KeiLanguage,
) {
    // テーマ / 言語状態の唯一の所有者。変更能力は onToggle* のコールバック配線でのみ配布する
    var isDark by remember(initialIsDark) { mutableStateOf(initialIsDark) }
    val onToggleTheme = remember { { isDark = !isDark } }
    var language by remember(initialLanguage) { mutableStateOf(initialLanguage) }
    val onToggleLanguage = remember {
        {
            language = if (language == KeiLanguage.Ja) KeiLanguage.En else KeiLanguage.Ja
        }
    }

    LaunchedEffect(language) { setDocumentLanguage(language.tag) }

    LaunchedEffect(appGraph) {
        snapshotFlow { isDark }
            .drop(1) // 初回 emission は復元値そのものなので保存しない
            .collect { value ->
                appGraph.interactionLog.d("Theme", "save isDark=$value")
                // 保存は best-effort: 失敗（quota 超過など）でも監視は続け、次回の切り替えで再度保存する
                runBestEffort { appGraph.themeRepository.saveIsDark(value) }
            }
    }

    LaunchedEffect(appGraph) {
        snapshotFlow { language }
            .drop(1)
            .collect { value ->
                appGraph.interactionLog.d("Language", "save languageTag=${value.tag}")
                runBestEffort { appGraph.languageRepository.saveLanguageTag(value.tag) }
            }
    }

    val defaultResourceReader = LocalResourceReader.current
    val retryingResourceReader = remember(defaultResourceReader) { RetryingResourceReader(defaultResourceReader) }
    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
        LocalResourceReader provides retryingResourceReader,
    ) {
        KeiLanguageResourceEnvironment(isDark = isDark, language = language) {
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

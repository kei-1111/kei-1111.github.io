package io.github.kei_1111.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.zacsweers.metro.createGraph
import io.github.kei_1111.app.core.designsystem.theme.KeiThemeController
import io.github.kei_1111.app.di.AppGraph
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appGraph = createGraph<AppGraph>()

    MainScope().launch {
        // 初回フレームから復元済みテーマで描画するため、ComposeViewport の前に保存値を待つ
        try {
            KeiThemeController.restore(appGraph.themeRepository.isDark.first())
        } catch (_: Exception) {
            // 意図的な握りつぶし: 復元失敗（localStorage 破損・アクセス不可など）で描画自体を
            // 止めず、KeiThemeController の初期値（ダーク）のまま起動する
        }
        ComposeViewport(document.body!!) {
            App(appGraph = appGraph)
        }
    }
}

package io.github.kei_1111.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.zacsweers.metro.createGraph
import io.github.kei_1111.app.core.common.coroutines.recoverOrElse
import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.core.designsystem.language.KeiLanguageController
import io.github.kei_1111.app.core.utils.browserLanguageTag
import io.github.kei_1111.app.di.AppGraph
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appGraph = createGraph<AppGraph>()
    KeiLanguageController.initialize(KeiLanguage.fromTag(browserLanguageTag()))
    installDoubleShiftListener()

    MainScope().launch {
        // 初回フレームから復元済みテーマで描画するため、ComposeViewport の前に保存値を待つ
        val initialIsDark = recoverOrElse({ appGraph.themeRepository.isDark.first() }) {
            // 復元失敗（localStorage 破損・アクセス不可など）で描画自体を止めず、初期値ダークで起動する
            true
        }
        ComposeViewport(document.body!!) {
            App(
                appGraph = appGraph,
                initialIsDark = initialIsDark,
            )
        }
    }
}

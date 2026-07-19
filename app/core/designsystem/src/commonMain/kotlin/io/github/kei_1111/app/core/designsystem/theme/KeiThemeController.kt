package io.github.kei_1111.app.core.designsystem.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * アプリ全体のテーマ（ダーク / ライト）を保持するグローバルコントローラ。
 * テーマは KeiTheme（App ルート）を横断して適用され、非 Composable コードも
 * [keiColorScheme] 経由でこの状態を参照するため、画面ローカルの MVI ではなく
 * アプリスコープのシングルトンで保持する。
 */
object KeiThemeController {
    /** true ならダーク（Islands Dark）、false ならライト（Islands Light）。初期値はダーク。 */
    var isDark: Boolean by mutableStateOf(true)
        private set

    /** ダーク / ライトを切り替える。選択の永続化は webApp 層（App.kt）が担う。 */
    fun toggle() {
        isDark = !isDark
    }

    /** 永続化された選択を復元する。webApp の起動時（ComposeViewport 前）に呼ばれる。 */
    fun restore(isDark: Boolean) {
        this.isDark = isDark
    }
}

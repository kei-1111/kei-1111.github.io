package io.github.kei_1111.core.designsystem.theme

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

    /** 選択は永続化されず、リロードで初期値（ダーク）に戻る。 */
    fun toggle() {
        isDark = !isDark
    }
}

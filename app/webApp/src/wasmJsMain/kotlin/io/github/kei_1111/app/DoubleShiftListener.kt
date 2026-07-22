package io.github.kei_1111.app

import io.github.kei_1111.app.navigation.SearchEverywhereController
import kotlinx.browser.document
import org.w3c.dom.events.KeyboardEvent

private const val DOUBLE_SHIFT_WINDOW_MILLIS = 500.0

/**
 * ダブル Shift で Search Everywhere を開く、実 AS 由来のグローバルショートカット。
 *
 * リスナーを解除しないのは、呼び出し元が `main()` の一箇所だけで、単一ページの wasm アプリでは
 * その寿命がページ自体の寿命と一致するため。
 */
internal fun installDoubleShiftListener() {
    var lastShiftUpAt = Double.NEGATIVE_INFINITY
    // Shift を他の修飾キーと組み合わせて使った押下（Shift+A、Ctrl+Shift など、押す順序は問わない）は
    // 「1回目の Shift」に数えない。これがないと組み合わせ後の単発 Shift が誤って開いてしまう。
    var shiftUsedAsModifier = false
    document.addEventListener("keydown", { event ->
        val keyboardEvent = event as KeyboardEvent
        when {
            keyboardEvent.repeat -> Unit
            keyboardEvent.key == "Shift" -> {
                if (keyboardEvent.timeStamp.toDouble() - lastShiftUpAt <= DOUBLE_SHIFT_WINDOW_MILLIS) {
                    lastShiftUpAt = Double.NEGATIVE_INFINITY
                    SearchEverywhereController.requestOpen()
                }
                shiftUsedAsModifier = keyboardEvent.ctrlKey || keyboardEvent.altKey || keyboardEvent.metaKey
            }

            else -> {
                shiftUsedAsModifier = true
                lastShiftUpAt = Double.NEGATIVE_INFINITY
            }
        }
    })
    document.addEventListener("keyup", { event ->
        val keyboardEvent = event as KeyboardEvent
        if (keyboardEvent.key == "Shift" && !shiftUsedAsModifier) {
            lastShiftUpAt = keyboardEvent.timeStamp.toDouble()
        }
    })
}

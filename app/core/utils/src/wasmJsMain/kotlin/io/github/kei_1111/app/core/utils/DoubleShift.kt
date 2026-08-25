package io.github.kei_1111.app.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.document
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

private const val KEY_DOWN_EVENT = "keydown"
private const val KEY_UP_EVENT = "keyup"
private const val DOUBLE_SHIFT_INTERVAL_MILLIS = 500.0

@Composable
actual fun DoubleShiftEffect(onDoubleShift: () -> Unit) {
    val currentOnDoubleShift by rememberUpdatedState(onDoubleShift)
    DisposableEffect(Unit) {
        var lastShiftUpMillis = Double.NEGATIVE_INFINITY
        // Shift を他の修飾キーと組み合わせて使った押下（Shift+A、Ctrl+Shift など、押す順序は問わない）は
        // 「1回目の Shift」に数えない。これがないと組み合わせ後の単発 Shift が誤って開いてしまう。
        var shiftUsedAsModifier = false
        val keyDown: (Event) -> Unit = { event ->
            val keyboardEvent = event as KeyboardEvent
            when {
                keyboardEvent.repeat -> Unit
                keyboardEvent.key == "Shift" -> {
                    if (keyboardEvent.timeStamp.toDouble() - lastShiftUpMillis <= DOUBLE_SHIFT_INTERVAL_MILLIS) {
                        lastShiftUpMillis = Double.NEGATIVE_INFINITY
                        currentOnDoubleShift()
                    }
                    shiftUsedAsModifier = keyboardEvent.ctrlKey || keyboardEvent.altKey || keyboardEvent.metaKey
                }

                else -> {
                    shiftUsedAsModifier = true
                    lastShiftUpMillis = Double.NEGATIVE_INFINITY
                }
            }
        }
        val keyUp: (Event) -> Unit = { event ->
            val keyboardEvent = event as KeyboardEvent
            if (keyboardEvent.key == "Shift" && !shiftUsedAsModifier) {
                lastShiftUpMillis = keyboardEvent.timeStamp.toDouble()
            }
        }
        document.addEventListener(KEY_DOWN_EVENT, keyDown)
        document.addEventListener(KEY_UP_EVENT, keyUp)
        onDispose {
            document.removeEventListener(KEY_DOWN_EVENT, keyDown)
            document.removeEventListener(KEY_UP_EVENT, keyUp)
        }
    }
}

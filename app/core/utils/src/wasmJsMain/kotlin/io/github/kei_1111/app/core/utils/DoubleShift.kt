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
        // Shift+A のような修飾利用を単独タップと区別する（間に他キーが挟まったら数えない）
        var interrupted = false
        val keyDown: (Event) -> Unit = { event ->
            if ((event as? KeyboardEvent)?.key != "Shift") interrupted = true
        }
        val keyUp: (Event) -> Unit = { event ->
            if ((event as? KeyboardEvent)?.key == "Shift") {
                val now = event.timeStamp.toDouble()
                if (!interrupted && now - lastShiftUpMillis <= DOUBLE_SHIFT_INTERVAL_MILLIS) {
                    lastShiftUpMillis = Double.NEGATIVE_INFINITY
                    currentOnDoubleShift()
                } else {
                    lastShiftUpMillis = if (interrupted) Double.NEGATIVE_INFINITY else now
                }
                interrupted = false
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

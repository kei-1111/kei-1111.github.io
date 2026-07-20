package io.github.kei_1111.app

import io.github.kei_1111.app.navigation.SearchEverywhereController
import kotlinx.browser.document
import org.w3c.dom.events.KeyboardEvent

private const val DOUBLE_SHIFT_WINDOW_MILLIS = 500.0

internal fun installDoubleShiftListener() {
    var lastShiftUpAt = Double.NEGATIVE_INFINITY
    document.addEventListener("keydown", { event ->
        val keyboardEvent = event as KeyboardEvent
        when {
            keyboardEvent.repeat -> Unit
            keyboardEvent.key == "Shift" -> {
                if (keyboardEvent.timeStamp.toDouble() - lastShiftUpAt <= DOUBLE_SHIFT_WINDOW_MILLIS) {
                    lastShiftUpAt = Double.NEGATIVE_INFINITY
                    SearchEverywhereController.requestOpen()
                }
            }

            else -> lastShiftUpAt = Double.NEGATIVE_INFINITY
        }
    })
    document.addEventListener("keyup", { event ->
        val keyboardEvent = event as KeyboardEvent
        if (keyboardEvent.key == "Shift") lastShiftUpAt = keyboardEvent.timeStamp.toDouble()
    })
}

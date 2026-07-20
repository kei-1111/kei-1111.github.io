package io.github.kei_1111.app.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal object SearchEverywhereController {
    var openTick: Int by mutableStateOf(0)
        private set

    fun requestOpen() {
        openTick++
    }
}

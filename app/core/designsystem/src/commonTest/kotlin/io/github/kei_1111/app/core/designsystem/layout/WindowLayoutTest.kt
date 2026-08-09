package io.github.kei_1111.app.core.designsystem.layout

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowLayoutTest {

    @Test
    fun resolvesMobileBelowTheBreakpoint() {
        assertEquals(WindowLayout.Mobile, windowLayoutFor(899.dp))
    }

    @Test
    fun resolvesDesktopAtTheBreakpoint() {
        assertEquals(WindowLayout.Desktop, windowLayoutFor(900.dp))
    }

    @Test
    fun resolvesDesktopAboveTheBreakpoint() {
        assertEquals(WindowLayout.Desktop, windowLayoutFor(1280.dp))
    }

    @Test
    fun resolvesMobileAtZeroWidth() {
        assertEquals(WindowLayout.Mobile, windowLayoutFor(0.dp))
    }
}

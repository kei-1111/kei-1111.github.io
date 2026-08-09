package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val DRAG_DISTANCE_PX = 80.0

/**
 * 下部ツールウィンドウの高さドラッグ。ハンドルは canvas 上の実ポインタでしか掴めないため、
 * testTag で特定した要素の座標から実マウス操作を行い、パネル内要素の移動量で結果を観測する。
 */
class BottomPanelResizeE2eTest : PlaywrightTestBase() {

    @Test
    fun draggingTheHandleGrowsThePanel() {
        val profile = ProfilePage(page)

        profile.toggleTerminalRail()
        assertThat(profile.terminalInput()).isVisible()
        val before = requireNotNull(profile.terminalInput().boundingBox()).y

        profile.dragBottomPanelHandle(upBy = DRAG_DISTANCE_PX)

        val after = requireNotNull(profile.terminalInput().boundingBox()).y
        // パネルが上へ伸びるぶん、内側の入力欄も上がる
        assertTrue(after < before, "expected the terminal input to move up, was $before -> $after")
    }
}

@file:Suppress("MagicNumber")

package io.github.kei_1111.core.designsystem.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 現在のウィンドウ幅に対応するレイアウト種別。 */
enum class WindowLayout {
    Desktop,
    Mobile,
}

/** Mobile レイアウトへ切り替えるブレークポイント。判定は [windowLayoutFor] に集約する。 */
private val CompactWidthBreakpoint = 900.dp

/** ウィンドウ幅から [WindowLayout] を求める。 */
fun windowLayoutFor(width: Dp): WindowLayout =
    if (width < CompactWidthBreakpoint) WindowLayout.Mobile else WindowLayout.Desktop

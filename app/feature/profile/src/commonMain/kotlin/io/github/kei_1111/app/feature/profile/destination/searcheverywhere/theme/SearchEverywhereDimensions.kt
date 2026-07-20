@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Search Everywhere パレットの寸法トークン。実 AS の Search Everywhere ポップアップに合わせる。 */
internal data object SearchEverywhereDimensions {
    val PanelMaxWidth = 720.dp
    val PanelMaxHeight = 660.dp
    val PanelHorizontalMargin = 48.dp
    const val PanelHeightFraction = 0.68f
    const val PanelTopFraction = 0.17f

    /** これより狭いとヘッダ右側の非対話クラスタ（チェックボックスとアイコン）を畳む。 */
    val PanelCompactWidth = 560.dp
    val PanelBorderWidth = 1.dp

    val HeaderHeight = 40.dp
    val FieldHeight = 32.dp
    val FieldBorderWidth = 2.dp
    val RowHeight = 26.dp
    val FooterHeight = 30.dp
    val DividerHeight = 1.dp

    val IconSize = 16.dp
    val CheckboxSize = 14.dp

    val TabFontSize = 12.sp
    val NameFontSize = 13.sp
    val DetailFontSize = 12.sp
    val CategoryFontSize = 11.sp
}

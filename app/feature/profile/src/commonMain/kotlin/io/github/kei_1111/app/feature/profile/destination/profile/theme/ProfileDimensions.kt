@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data object ProfileDimensions {
    val DeskPadding = 10.dp
    val IslandGap = 7.dp
    val RailWidth = 30.dp // 実 AS 実測: アイコンピル 30px がレール幅いっぱい
    val RailMargin = 5.dp // 実 AS 実測: ウィンドウ端 → ピル左端 5px
    val TreeWidth = 248.dp

    /** Desktop / Mobile 共通。 */
    val LogcatPanelHeight = 260.dp

    /** Desktop / Mobile 共通。 */
    val TodoPanelHeight = 260.dp

    /** Desktop / Mobile 共通。 */
    val TerminalPanelHeight = 260.dp

    val BottomPanelMinHeight = 120.dp

    val ChromePillSize = 30.dp
    val ChromeIconSize = 16.dp

    /** パネル内ツールバーの小アイコン。 */
    val ChromeIconSizeSmall = 12.dp
    val RailIconSize = 20.dp
    val TitleBarIconSize = 18.dp
    val ChromeLabelFontSize = 12.sp

    val TreeLeftInset = 6.dp
    val TreeChevronSize = 16.dp
    val TreeIconSize = 16.dp
    val TreeChevronGap = 3.dp
    val TreeIconLabelGap = 6.dp
    val TreeIndentStep = TreeChevronSize + TreeChevronGap // 子の矢印が親アイコン真下に整列

    val GitHubCardWidth = 280.dp
    val GitHubCardHeight = 600.dp
    val GitHubCardPadding = 20.dp
    val CardSectionGap = 14.dp

    val LicenseCardWidth = 280.dp
    val LicenseCardHeight = 600.dp
    val LicenseCardPadding = 18.dp

    val WorksCardWidth = 280.dp
    val WorksCardHeight = 600.dp
    val WorksCardPadding = 18.dp

    val ZoomControlButtonSize = 24.dp
    val ZoomControlGroupGap = 6.dp

    val EditorLineHeight = 22.dp
    val EditorCaretWidth = 2.dp // 実 AS 実測: 2dp 幅 × 18dp 高
    val EditorCaretVerticalInset = 2.dp // 行高 22dp とキャレット高 18dp の差の半分
    val SplitHandleHitWidth = 9.dp
    val ScrollbarThickness = 8.dp
    val ScrollbarMinThumbLength = 24.dp

    val SkeletonBarHeight = 10.dp
    val SkeletonIndentStep = 16.dp

    val PreviewSpinnerSize = 28.dp
    val PreviewSpinnerStrokeWidth = 3.dp
    val PreviewBuildingBarWidth = 90.dp
    val PreviewBuildingBarHeight = 3.dp

    const val DefaultEditorPaneFraction = 1.25f / 2.25f

    const val MinPaneFraction = 0.2f

    const val MaxPaneFraction = 0.8f

    /** シート高さ（カード高さに対する割合）。 */
    const val SheetHeightFraction = 0.62f

    /** Works 詳細シートの高さ（カード高さに対する割合）。本文が長いため License より高めに取る。 */
    const val WorksSheetHeightFraction = 0.76f

    /** 下部ツールウィンドウ（Logcat / TODO / Terminal）がワークスペース高に占められる最大比。上段のエディタ行の最小高を確保する。 */
    const val MaxBottomPanelHeightFraction = 0.7f
}

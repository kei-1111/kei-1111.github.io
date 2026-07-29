@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** IDE レイアウト共通の寸法トークン。 */
internal data object ProfileDimensions {
    val DeskPadding = 10.dp
    val IslandGap = 7.dp
    val RailWidth = 30.dp // 実 AS 実測: アイコンピル 30px がレール幅いっぱい
    val RailMargin = 5.dp // 実 AS 実測: ウィンドウ端 → ピル左端 5px
    val TreeWidth = 248.dp

    /** 下部 Logcat ツールウィンドウの初期高（Desktop / Mobile 共通）。 */
    val LogcatPanelHeight = 260.dp

    /** ドラッグリサイズ時の Logcat ツールウィンドウの最小高。 */
    val LogcatPanelMinHeight = 120.dp

    // クローム（タイトルバー / レール）アイコンボタン共通
    val ChromePillSize = 30.dp
    val ChromeIconSize = 16.dp
    val RailIconSize = 20.dp
    val TitleBarIconSize = 18.dp
    val ChromeLabelFontSize = 12.sp

    // プロジェクトツリー行
    val TreeLeftInset = 6.dp
    val TreeChevronSize = 16.dp
    val TreeIconSize = 16.dp
    val TreeChevronGap = 3.dp
    val TreeIconLabelGap = 6.dp
    val TreeIndentStep = TreeChevronSize + TreeChevronGap // = 19.dp。子の矢印が親アイコン真下に整列

    // GitHub プロフィールカード
    val GitHubCardWidth = 280.dp
    val GitHubCardHeight = 600.dp
    val GitHubCardPadding = 20.dp
    val GitHubCardSectionGap = 14.dp

    // ライセンスカード
    val LicenseCardWidth = 280.dp
    val LicenseCardHeight = 600.dp
    val LicenseCardPadding = 18.dp

    // プレビューのズームコントロール
    val ZoomControlButtonSize = 24.dp

    /** ズームコントロールの2つのコンテナ間の縦ギャップ。 */
    val ZoomControlGroupGap = 6.dp

    // エディタ
    val EditorLineHeight = 22.dp
    val EditorCaretWidth = 2.dp // 実 AS 実測: 2dp 幅 × 18dp 高
    val EditorCaretVerticalInset = 2.dp // 行高 22dp とキャレット高 18dp の差の半分
    val SplitHandleHitWidth = 9.dp
    val ScrollbarThickness = 8.dp
    val ScrollbarMinThumbLength = 24.dp

    // エディタコードスケルトン（シマー）
    val SkeletonBarHeight = 10.dp
    val SkeletonIndentStep = 16.dp

    // Preview ビルド中インジケータ
    val PreviewSpinnerSize = 28.dp
    val PreviewSpinnerStrokeWidth = 3.dp
    val PreviewBuildingBarWidth = 90.dp
    val PreviewBuildingBarHeight = 3.dp

    // レイアウト比率

    /** エディタペインの初期幅比。 */
    const val DefaultEditorPaneFraction = 1.25f / 2.25f

    /** エディタペインの最小幅比。 */
    const val MinPaneFraction = 0.2f

    /** エディタペインの最大幅比。 */
    const val MaxPaneFraction = 0.8f

    /** シート高さ（カード高さに対する割合）。 */
    const val SheetHeightFraction = 0.62f

    /** Logcat がワークスペース高に占められる最大比。上段のエディタ行の最小高を確保する。 */
    const val MaxLogcatHeightFraction = 0.7f
}

@file:Suppress("MagicNumber")

package io.github.kei_1111.feature.profile.theme

import androidx.compose.ui.unit.dp

/** IDE レイアウト共通の寸法トークン。 */
internal data object ProfileDimensions {
    val DeskPadding = 10.dp
    val IslandGap = 7.dp
    val RailWidth = 30.dp // 実 AS 実測: アイコンピル 30px がレール幅いっぱい
    val RailMargin = 5.dp // 実 AS 実測: ウィンドウ端 → ピル左端 5px
    val TreeWidth = 248.dp

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
}

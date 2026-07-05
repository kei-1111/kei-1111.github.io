@file:Suppress("MagicNumber")

package io.github.kei_1111.feature.splash.theme

import androidx.compose.ui.graphics.Color
import io.github.kei_1111.core.designsystem.theme.IdeColors

/**
 * スプラッシュ（Android Studio 起動画面風）専用のカラートークン。
 * 起動画面は本体の Islands Dark よりワントーン暗い配色を使う。
 */
internal data object SplashColors {
    /** 最背面の全面ベタ背景 */
    val Desk = Color(0xFF141419)

    val Card = Color(0xFF1E1F25)
    val CardBorder = Color(0xFF26272F)

    val TextTitle = Color(0xFFDFE0EA)
    val TextDim = Color(0xFF6C6D78)
    val TextLog = Color(0xFF9C9DAA)

    val ProgressTrack = Color(0xFF26272F)
    val ProgressBar = IdeColors.AndroidGreen
    val ProgressBarFailed = IdeColors.Error

    val StatusRunning = IdeColors.Warning
    val StatusDone = IdeColors.Success
    val StatusFailed = IdeColors.Error
}

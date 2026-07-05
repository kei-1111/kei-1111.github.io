@file:Suppress("MagicNumber")

package io.github.kei_1111.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Android Studio "New UI / Islands Dark" テーマを再現するためのカラースキーム。
 * 値は実際の Android Studio (Islands Dark) のスクリーンショット実測値に合わせている。
 */
@Immutable
data class KeiColorScheme(
    // IDE クローム
    val desk: Color,
    val deskGlow: Color,
    val island: Color,
    val islandDark: Color,
    val islandBorder: Color,
    val selectionPill: Color,
    val tabSelected: Color,
    val tabSelectedBorder: Color,
    val chip: Color,
    val deskChip: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textCode: Color,

    val muted: Color,
    val mutedMid: Color,
    val mutedHigh: Color,

    val success: Color,
    val warning: Color,
    val error: Color,

    // Kotlin シンタックスハイライト（実 AS スクリーンショット実測値）
    val syntaxKeyword: Color,
    val syntaxAnnotation: Color,
    val syntaxFunction: Color,
    val syntaxComposableCall: Color,
    val syntaxEnumEntry: Color,
    val syntaxString: Color,
    val syntaxNumber: Color,
    val syntaxNamedArg: Color,
    val syntaxComment: Color,
    val syntaxLink: Color,

    // ブランドアクセント（コンテンツ側）
    val androidGreen: Color,
    val onAndroidGreen: Color,
    val workTag: Color,

    // Preview カード
    val cardBackground: Color,

    // GitHub プロフィールカード（Preview コンテンツ側）
    val gitHubItem: Color,
    val gitHubItemHover: Color,
    val brandQiita: Color,
    val brandZenn: Color,
    val langKotlin: Color,
    val langSwift: Color,
    val langShell: Color,

    /** Contributions ヒートマップ（Less → More の5段階）。 */
    val contributionLevels: List<Color>,

    // スプラッシュ（Android Studio 起動画面風）専用のカラートークン
    val splashDesk: Color,
    val splashCard: Color,
    val splashCardBorder: Color,
    val splashTextTitle: Color,
    val splashTextDim: Color,
    val splashTextLog: Color,
    val splashProgressTrack: Color,
    val splashProgressBar: Color,
    val splashProgressBarFailed: Color,
    val splashStatusRunning: Color,
    val splashStatusDone: Color,
    val splashStatusFailed: Color,
)

val keiColorScheme = KeiColorScheme(
    // IDE クローム
    desk = Color(0xFF26282C),
    deskGlow = Color(0xFF384164),
    island = Color(0xFF1E1F22),
    islandDark = Color(0xFF191A1C),
    islandBorder = Color(0xFF26282C),
    selectionPill = Color(0xFF33353A),
    tabSelected = Color(0xFF273555),
    tabSelectedBorder = Color(0xFF314679),
    chip = Color(0xFF2A2C30),
    deskChip = Color(0x1FFFFFFF),

    textPrimary = Color(0xFFDFE1E5),
    textSecondary = Color(0xFF9DA0A6),
    textCode = Color(0xFFBCBEC4),

    muted = Color(0xFF4B5059),
    mutedMid = Color(0xFF5A5D63),
    mutedHigh = Color(0xFF6F737A),

    success = Color(0xFF57965C),
    warning = Color(0xFFD5AE57),
    error = Color(0xFFDB5C5C),

    // Kotlin シンタックスハイライト（実 AS スクリーンショット実測値）
    syntaxKeyword = Color(0xFFCF8E6D),
    syntaxAnnotation = Color(0xFFB3AE60),
    syntaxFunction = Color(0xFF56A8F5),
    syntaxComposableCall = Color(0xFF6CB28B),
    syntaxEnumEntry = Color(0xFFC77DBB),
    syntaxString = Color(0xFF6AAB73),
    syntaxNumber = Color(0xFF2AACB8),
    syntaxNamedArg = Color(0xFF56C1D6),
    syntaxComment = Color(0xFF7A7E85),
    syntaxLink = Color(0xFF56A8F5),

    // ブランドアクセント（コンテンツ側）
    androidGreen = Color(0xFF3DDC84),
    onAndroidGreen = Color(0xFF0B1F14),
    workTag = Color(0xFF6AAB73),

    // Preview カード
    cardBackground = Color(0xFF1A1B1E),

    // GitHub プロフィールカード（Preview コンテンツ側）
    gitHubItem = Color(0xFF1F2124),
    gitHubItemHover = Color(0xFF292B2F),
    brandQiita = Color(0xFF55C500),
    brandZenn = Color(0xFF3EA8FF),
    langKotlin = Color(0xFFA97BFF),
    langSwift = Color(0xFFF05138),
    langShell = Color(0xFF89E051),

    contributionLevels = listOf(
        Color(0xFF1F2124),
        Color(0xFF173527),
        Color(0xFF1D4429),
        Color(0xFF2C6B3F),
        Color(0xFF3DDC84),
    ),

    // スプラッシュ（Android Studio 起動画面風）専用のカラートークン
    splashDesk = Color(0xFF141419),
    splashCard = Color(0xFF1E1F25),
    splashCardBorder = Color(0xFF26272F),
    splashTextTitle = Color(0xFFDFE0EA),
    splashTextDim = Color(0xFF6C6D78),
    splashTextLog = Color(0xFF9C9DAA),
    splashProgressTrack = Color(0xFF26272F),
    splashProgressBar = Color(0xFF3DDC84),
    splashProgressBarFailed = Color(0xFFDB5C5C),
    splashStatusRunning = Color(0xFFD5AE57),
    splashStatusDone = Color(0xFF57965C),
    splashStatusFailed = Color(0xFFDB5C5C),
)

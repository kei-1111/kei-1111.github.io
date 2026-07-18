@file:Suppress("MagicNumber")

package io.github.kei_1111.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Contributions ヒートマップの段階数（Less → More）。[KeiColorScheme.contributionLevels] のサイズ不変条件。 */
private const val CONTRIBUTION_LEVEL_COUNT = 5

/**
 * Android Studio "New UI" の配色トークンを保持する器（データクラス）。
 * 実インスタンスは [KeiDarkColorScheme] / [KeiLightColorScheme] としてそれぞれ定義される。
 */
@Immutable
data class KeiColorScheme(
    // IDE クローム
    val desk: Color,
    val deskGlow: Color,
    val island: Color,
    val islandDark: Color,
    val outline: Color,
    val selectionPill: Color,
    val tabSelected: Color,
    val tabSelectedBorder: Color,
    val chip: Color,
    val deskChip: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textCode: Color,

    val muted: Color,
    val mutedHigh: Color,

    // Kotlin シンタックスハイライト
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

    // Preview カード
    val cardBackground: Color,

    // GitHub プロフィールカード（Preview コンテンツ側）
    val gitHubItem: Color,
    val gitHubItemHover: Color,
    val brandQiita: Color,
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
) {
    init {
        require(contributionLevels.size == CONTRIBUTION_LEVEL_COUNT) {
            "contributionLevels は $CONTRIBUTION_LEVEL_COUNT 段階（Less→More）でなければなりません（実際: ${contributionLevels.size}）"
        }
    }
}

/** Android Studio "New UI / Islands Dark" の実スクリーンショット実測値。 */
val KeiDarkColorScheme = KeiColorScheme(
    // IDE クローム
    desk = Color(0xFF26282C),
    deskGlow = Color(0xFF384164),
    island = Color(0xFF1E1F22),
    islandDark = Color(0xFF191A1C),
    outline = Color(0xFF26282C),
    selectionPill = Color(0xFF33353A),
    tabSelected = Color(0xFF273555),
    tabSelectedBorder = Color(0xFF314679),
    chip = Color(0xFF2A2C30),
    deskChip = Color(0x1FFFFFFF),

    textPrimary = Color(0xFFDFE1E5),
    textSecondary = Color(0xFF9DA0A6),
    textCode = Color(0xFFBCBEC4),

    muted = Color(0xFF4B5059),
    mutedHigh = Color(0xFF6F737A),

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

    // Preview カード
    cardBackground = Color(0xFF1A1B1E),

    // GitHub プロフィールカード（Preview コンテンツ側）
    gitHubItem = Color(0xFF1F2124),
    gitHubItemHover = Color(0xFF292B2F),
    brandQiita = Color(0xFF55C500),
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

/**
 * Android Studio "New UI / Islands Light" テーマを再現するためのカラースキーム。
 * 値は実際の Android Studio (Islands Light) のスクリーンショット実測値に合わせている。
 */
val KeiLightColorScheme = KeiColorScheme(
    // IDE クローム（Islands Light）
    desk = Color(0xFFE9EAEE),
    deskGlow = Color(0xFFE9EAEE), // 実 AS Light はグロー無しの均一デスク（desk と同値）
    island = Color(0xFFFFFFFF),
    islandDark = Color(0xFFFFFFFF),
    outline = Color(0xFFEBECF0),
    selectionPill = Color(0xFFD5D8DE),
    tabSelected = Color(0xFFE3EBFE),
    tabSelectedBorder = Color(0xFFA7C5FF),
    chip = Color(0xFFEBECF0),
    deskChip = Color(0x14000000),

    textPrimary = Color(0xFF080808),
    textSecondary = Color(0xFF6C707E),
    textCode = Color(0xFF080808),

    muted = Color(0xFFB9BCC4),
    mutedHigh = Color(0xFF6C707E),

    // Kotlin シンタックスハイライト（IntelliJ Light 既定スキーム）
    syntaxKeyword = Color(0xFF0033B3),
    syntaxAnnotation = Color(0xFF9E880D),
    syntaxFunction = Color(0xFF00627A),
    syntaxComposableCall = Color(0xFF009900),
    syntaxEnumEntry = Color(0xFF871094),
    syntaxString = Color(0xFF067D17),
    syntaxNumber = Color(0xFF1750EB),
    syntaxNamedArg = Color(0xFF4A86E8),
    syntaxComment = Color(0xFF8C8C8C),
    syntaxLink = Color(0xFF0033B3),

    // ブランドアクセント（コンテンツ側）— 明暗で不変
    androidGreen = Color(0xFF3DDC84),

    // Preview カード
    cardBackground = Color(0xFFFFFFFF),

    // GitHub プロフィールカード（Preview コンテンツ側）
    gitHubItem = Color(0xFFF6F8FA),
    gitHubItemHover = Color(0xFFEEF1F4),
    brandQiita = Color(0xFF55C500),
    langKotlin = Color(0xFFA97BFF),
    langSwift = Color(0xFFF05138),
    langShell = Color(0xFF89E051),

    contributionLevels = listOf(
        Color(0xFFEBEDF0),
        Color(0xFF9BE9A8),
        Color(0xFF40C463),
        Color(0xFF30A14E),
        Color(0xFF216E39),
    ),

    // スプラッシュ（実 AS 起動画面は常にダーク基調のため、ライトでもダーク値を維持）
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

/**
 * 現在アクティブなカラースキーム。[KeiThemeController.isDark] に追従する computed プロパティ。
 * 非 Composable コード（deskBackground など）はこれを参照する。
 * snapshot state を読むため、描画・コンポジション中の参照はテーマ切替で再実行される。
 * ただし `remember` 等でキャッシュする計算の内側で読む場合は、`KeiTheme.colors` などをキーに
 * 含めないとテーマ切替に追従しない点に注意（キャッシュされた計算結果自体は再実行されないため）。
 */
val keiColorScheme: KeiColorScheme
    get() = if (KeiThemeController.isDark) KeiDarkColorScheme else KeiLightColorScheme

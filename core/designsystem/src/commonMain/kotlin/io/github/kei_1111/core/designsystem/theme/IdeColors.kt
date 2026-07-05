@file:Suppress("MagicNumber")

package io.github.kei_1111.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kei_1111.core.designsystem.generated.resources.Res
import kei_1111.core.designsystem.generated.resources.jetbrains_mono_bold
import kei_1111.core.designsystem.generated.resources.jetbrains_mono_medium
import kei_1111.core.designsystem.generated.resources.jetbrains_mono_regular
import kei_1111.core.designsystem.generated.resources.noto_sans_jp_medium
import kei_1111.core.designsystem.generated.resources.noto_sans_jp_semi_bold
import kei_1111.core.designsystem.generated.resources.zen_kaku_gothic_new_bold
import kei_1111.core.designsystem.generated.resources.zen_kaku_gothic_new_regular
import org.jetbrains.compose.resources.Font

/**
 * Android Studio "New UI / Islands Dark" テーマを再現するためのカラートークン。
 * 値は実際の Android Studio (Islands Dark) のスクリーンショット実測値に合わせている。
 */
data object IdeColors {
    // IDE クローム
    val Desk = Color(0xFF26282C) // 最背面のデスク（ウィンドウ背景。タイトル/ステータスバー・レールもこの上に直接乗る）
    val DeskGlow = Color(0xFF384164) // デスク左上のブルーグロー（グラデーションのピーク色）
    val Island = Color(0xFF1E1F22) // エディタ・プレビューの島
    val IslandDark = Color(0xFF191A1C) // プロジェクトツリーの島（エディタより暗い）
    val IslandBorder = Color(0xFF26282C) // 島内の区切り線・チップの枠線
    val SelectionPill = Color(0xFF33353A) // 選択ツリー行などのグレーピル
    val TabSelected = Color(0xFF273555) // 選択エディタタブの塗り
    val TabSelectedBorder = Color(0xFF314679) // 選択エディタタブの枠線
    val Chip = Color(0xFF2A2C30) // 島の上に乗るピル・チップ類の背景
    val DeskChip = Color(0x1FFFFFFF) // デスク（グラデーション領域含む）に乗るピル。半透明ホワイト

    val TextPrimary = Color(0xFFDFE1E5)
    val TextSecondary = Color(0xFF9DA0A6)
    val TextCode = Color(0xFFBCBEC4)

    val Muted = Color(0xFF4B5059)
    val MutedMid = Color(0xFF5A5D63)
    val MutedHigh = Color(0xFF6F737A)

    val Success = Color(0xFF57965C)
    val Warning = Color(0xFFD5AE57)
    val Error = Color(0xFFDB5C5C)

    // Kotlin シンタックスハイライト（実 AS スクリーンショット実測値）
    val SyntaxKeyword = Color(0xFFCF8E6D) // キーワード
    val SyntaxAnnotation = Color(0xFFB3AE60) // アノテーション
    val SyntaxFunction = Color(0xFF56A8F5) // 関数宣言名
    val SyntaxComposableCall = Color(0xFF6CB28B) // Composable 関数の呼び出し（AS 固有の緑）
    val SyntaxEnumEntry = Color(0xFFC77DBB) // enum エントリ（イタリック）
    val SyntaxString = Color(0xFF6AAB73) // 文字列リテラル
    val SyntaxNumber = Color(0xFF2AACB8) // 数値リテラル
    val SyntaxNamedArg = Color(0xFF56C1D6) // 名前付き引数（シアン）
    val SyntaxComment = Color(0xFF7A7E85) // コメント
    val SyntaxLink = Color(0xFF56A8F5) // リンク

    // ブランドアクセント（コンテンツ側）
    val AndroidGreen = Color(0xFF3DDC84)
    val OnAndroidGreen = Color(0xFF0B1F14)
    val WorkTag = Color(0xFF6AAB73)

    // Preview カード
    val CardBackground = Color(0xFF1A1B1E)

    // GitHub プロフィールカード（Preview コンテンツ側）
    val GitHubItem = Color(0xFF1F2124) // 行アイテム・チップ背景
    val GitHubItemHover = Color(0xFF292B2F) // ホバー時にワントーン明るく
    val BrandQiita = Color(0xFF55C500) // Qiita ブランド緑
    val BrandZenn = Color(0xFF3EA8FF) // Zenn ブランド青
    val LangKotlin = Color(0xFFA97BFF)
    val LangSwift = Color(0xFFF05138)
    val LangShell = Color(0xFF89E051)

    /** Contributions ヒートマップ（Less → More の5段階）。 */
    val ContributionLevels = listOf(
        Color(0xFF1F2124),
        Color(0xFF173527),
        Color(0xFF1D4429),
        Color(0xFF2C6B3F),
        Color(0xFF3DDC84),
    )
}

/**
 * コード・IDE クローム用の等幅フォント（JetBrains Mono）。
 * 末尾に Noto Sans JP を並べ、JetBrains Mono に無い日本語グリフをフォールバックさせる。
 * (resource, weight) の組を変更したら FontPreload.kt のプリロードも更新すること。
 */
@Composable
fun JetBrainsMonoFamily() = FontFamily(
    Font(
        resource = Res.font.jetbrains_mono_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        resource = Res.font.jetbrains_mono_medium,
        weight = FontWeight.Medium,
    ),
    Font(
        resource = Res.font.jetbrains_mono_bold,
        weight = FontWeight.Bold,
    ),
    Font(
        resource = Res.font.noto_sans_jp_medium,
        weight = FontWeight.Normal,
    ),
    Font(
        resource = Res.font.noto_sans_jp_semi_bold,
        weight = FontWeight.Bold,
    ),
)

/**
 * コード内の日本語区間へ明示適用するフォント。
 * wasm ではフォールバック解決を伴う計測が実描画より狭くなり `softWrap = false` でも
 * 日本語行が折り返されるため、日本語区間にはこのファミリーを明示指定して計測を確定させる。
 * (resource, weight) の組は [JetBrainsMonoFamily] のフォールバック枠と一致させること
 * （FontPreload.kt が同じ組をプリロードしている）。
 */
@Composable
fun CodeJapaneseFallbackFamily() = FontFamily(
    Font(
        resource = Res.font.noto_sans_jp_medium,
        weight = FontWeight.Normal,
    ),
    Font(
        resource = Res.font.noto_sans_jp_semi_bold,
        weight = FontWeight.Bold,
    ),
)

/**
 * GitHub プロフィールカードの日本語テキスト用フォント（Zen Kaku Gothic New）。
 * (resource, weight) の組を変更したら FontPreload.kt のプリロードも更新すること。
 */
@Composable
fun ZenKakuGothicNewFamily() = FontFamily(
    Font(
        resource = Res.font.zen_kaku_gothic_new_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        resource = Res.font.zen_kaku_gothic_new_bold,
        weight = FontWeight.Bold,
    ),
)

/**
 * 日本語・カード内テキスト用フォント（Noto Sans JP）。
 * (resource, weight) の組を変更したら FontPreload.kt のプリロードも更新すること。
 */
@Composable
fun IdeJapaneseFamily() = FontFamily(
    Font(
        resource = Res.font.noto_sans_jp_medium,
        weight = FontWeight.Medium,
    ),
    Font(
        resource = Res.font.noto_sans_jp_semi_bold,
        weight = FontWeight.SemiBold,
    ),
)

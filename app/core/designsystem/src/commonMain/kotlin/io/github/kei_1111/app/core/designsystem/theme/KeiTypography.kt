@file:Suppress("MagicNumber")

package io.github.kei_1111.app.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** IDE / スプラッシュ全体で使うテキストスタイルのセット。 */
@Immutable
data class KeiTypography(
    val code: TextStyle,
    val chrome: TextStyle,
    val cardJp: TextStyle,
    val githubJp: TextStyle,
    val mono: TextStyle,
)

@Composable
fun keiTypography(): KeiTypography = KeiTypography(
    // コード本文用の等幅スタイル。
    code = TextStyle(
        fontFamily = JetBrainsMonoFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 22.sp, // 実 AS 実測: 13px フォントで行ピッチ 22px
        color = keiColorScheme.textCode,
    ),
    // IDE クローム用の等幅 UI テキスト。
    chrome = TextStyle(
        fontFamily = JetBrainsMonoFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = keiColorScheme.textSecondary,
    ),
    // カード内の日本語テキスト用スタイル。
    cardJp = TextStyle(
        fontFamily = IdeJapaneseFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        color = keiColorScheme.textPrimary,
    ),
    // GitHub プロフィールカード内の日本語テキスト用スタイル（Zen Kaku Gothic New）。
    githubJp = TextStyle(
        fontFamily = ZenKakuGothicNewFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
        color = keiColorScheme.textPrimary,
    ),
    // 素の等幅ベーススタイル（スプラッシュ用）。サイズ・色は呼び出し側で指定する。
    mono = TextStyle(
        fontFamily = JetBrainsMonoFamily(),
    ),
)

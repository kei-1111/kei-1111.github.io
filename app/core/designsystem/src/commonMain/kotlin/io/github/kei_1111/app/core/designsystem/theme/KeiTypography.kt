@file:Suppress("MagicNumber")

package io.github.kei_1111.app.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
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
fun keiTypography(colors: KeiColorScheme): KeiTypography = KeiTypography(
    // コード本文用の等幅スタイル。
    code = TextStyle(
        fontFamily = JetBrainsMonoFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 22.sp, // 実 AS 実測: 13px フォントで行ピッチ 22px
        // Mode.Fixed で行ボックスを 22sp に強制する。日本語区間へ適用するフォールバックフォント
        // （SyntaxHighlighter.withJapaneseFont）の行メトリクスが 22sp をわずかに超え、
        // 密度によっては日本語を含む行だけ高くなり言語切替で行位置がずれるため。
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
            mode = LineHeightStyle.Mode.Fixed,
        ),
        color = colors.textCode,
    ),
    // IDE クローム用の等幅 UI テキスト。
    chrome = TextStyle(
        fontFamily = JetBrainsMonoFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = colors.textSecondary,
    ),
    // カード内の日本語テキスト用スタイル。
    cardJp = TextStyle(
        fontFamily = IdeJapaneseFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        color = colors.textPrimary,
    ),
    // GitHub プロフィールカード内の日本語テキスト用スタイル（Zen Kaku Gothic New）。
    githubJp = TextStyle(
        fontFamily = ZenKakuGothicNewFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
        color = colors.textPrimary,
    ),
    // 素の等幅ベーススタイル（スプラッシュ用）。サイズ・色は呼び出し側で指定する。
    mono = TextStyle(
        fontFamily = JetBrainsMonoFamily(),
    ),
)

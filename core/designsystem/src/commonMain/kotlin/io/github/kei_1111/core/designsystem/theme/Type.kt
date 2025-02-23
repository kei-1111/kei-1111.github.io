@file:Suppress("MagicNumber")

package io.github.kei_1111.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import kei_1111.core.designsystem.generated.resources.Res
import kei_1111.core.designsystem.generated.resources.noto_sans_jp_medium
import kei_1111.core.designsystem.generated.resources.noto_sans_jp_semi_bold
import org.jetbrains.compose.resources.Font

@Composable
fun NotoSansJpFamily() = FontFamily(
    Font(
        resource = Res.font.noto_sans_jp_medium,
        weight = FontWeight.Medium,
    ),
    Font(
        resource = Res.font.noto_sans_jp_semi_bold,
        weight = FontWeight.SemiBold,
    ),
)

@Composable
fun typography() = Typography(
    headlineLarge = TextStyle(
        fontFamily = NotoSansJpFamily(),
        fontWeight = FontWeight.SemiBold,
        fontSize = 48.sp,
        lineHeight = 52.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoSansJpFamily(),
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansJpFamily(),
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = NotoSansJpFamily(),
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansJpFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Top,
            trim = LineHeightStyle.Trim.Both,
        ),
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSansJpFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
)

@file:Suppress("MagicNumber")

package org.example.project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.noto_sans_jp_medium
import kei_1111.composeapp.generated.resources.noto_sans_jp_regular
import kei_1111.composeapp.generated.resources.noto_sans_jp_semi_bold
import org.jetbrains.compose.resources.Font

@Composable
fun NotoSansJpFamily() = FontFamily(
    Font(
        resource = Res.font.noto_sans_jp_regular,
        weight = FontWeight.Normal,
    ),
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
    titleLarge = TextStyle(
        fontFamily = NotoSansJpFamily(),
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansJpFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansJpFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Top,
            trim = LineHeightStyle.Trim.Both,
        ),
    ),
    labelLarge = TextStyle(
        fontFamily = NotoSansJpFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
    ),
)

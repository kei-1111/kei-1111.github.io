package io.github.kei_1111.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalKeiColorScheme = staticCompositionLocalOf { keiColorScheme }
val LocalKeiTypography = staticCompositionLocalOf<KeiTypography> { error("KeiTheme を経由せず KeiTypography が参照されました") }
val LocalKeiShapes = staticCompositionLocalOf { keiShapes }

@Composable
fun KeiTheme(
    colorScheme: KeiColorScheme = keiColorScheme,
    typography: KeiTypography = keiTypography(),
    shapes: KeiShapes = keiShapes,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalKeiColorScheme provides colorScheme,
        LocalKeiTypography provides typography,
        LocalKeiShapes provides shapes,
        content = content,
    )
}

object KeiTheme {
    val colors: KeiColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalKeiColorScheme.current
    val typography: KeiTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalKeiTypography.current
    val shapes: KeiShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalKeiShapes.current
}

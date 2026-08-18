package io.github.kei_1111.app.core.utils

import androidx.compose.ui.graphics.Color

// Android ターゲットは IDE の Compose Preview 描画専用（配布物は wasmJs のみ）。
// document / localStorage は存在しないため、実装は持たない。
actual fun setBrowserThemeColor(color: Color) = Unit

actual fun saveBootThemeColor(color: Color) = Unit

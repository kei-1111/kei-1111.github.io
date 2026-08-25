package io.github.kei_1111.app.core.utils

import androidx.compose.runtime.Composable

// Android ターゲットは IDE の Compose Preview 描画専用（配布物は wasmJs のみ）。
// 静的プレビューにキーボードは届かないため何もしない。
@Composable
actual fun DoubleShiftEffect(onDoubleShift: () -> Unit) = Unit

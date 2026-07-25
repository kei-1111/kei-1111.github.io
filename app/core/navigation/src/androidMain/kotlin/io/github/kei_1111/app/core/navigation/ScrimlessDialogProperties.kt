package io.github.kei_1111.app.core.navigation

import androidx.compose.ui.window.DialogProperties

// Android ターゲットは IDE の Compose Preview 描画専用（配布物は wasmJs のみ）。
// androidx の DialogProperties は scrimColor を持たないため、幅の指定だけを引き継ぐ。
actual fun scrimlessDialogProperties(): DialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
)

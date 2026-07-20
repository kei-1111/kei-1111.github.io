package io.github.kei_1111.app.core.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

actual fun scrimlessDialogProperties(): DialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
    scrimColor = Color.Transparent,
)

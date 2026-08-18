package io.github.kei_1111.app.core.utils

import androidx.compose.ui.graphics.Color

expect fun setBrowserThemeColor(color: Color)

/** 次回読み込み時、wasm クライアント起動前のペイントに使う色を残す。 */
expect fun saveBootThemeColor(color: Color)

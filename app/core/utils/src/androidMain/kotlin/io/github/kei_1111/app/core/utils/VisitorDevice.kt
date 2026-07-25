package io.github.kei_1111.app.core.utils

// Android ターゲットは IDE の Compose Preview 描画専用（配布物は wasmJs のみ）。
actual fun visitorDeviceLabel(): String = "Compose Preview"

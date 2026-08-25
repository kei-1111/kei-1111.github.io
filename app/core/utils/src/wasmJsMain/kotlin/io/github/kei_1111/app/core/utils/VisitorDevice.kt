package io.github.kei_1111.app.core.utils

private fun userAgent(): String = js("navigator.userAgent")

actual fun visitorDeviceLabel(): String = visitorDeviceLabel(userAgent())

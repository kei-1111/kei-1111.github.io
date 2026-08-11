package io.github.kei_1111.app.core.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsException

actual fun setBrowserThemeColor(color: Color) {
    document.querySelector("meta[name=theme-color]")?.setAttribute("content", color.toCssHex())
}

// localStorage の quota 超過・保存無効は Exception を継承しない JsException(Throwable 直下)で届き、
// そのままでは呼び出し側の runBestEffort をすり抜けるため Exception に正規化する。
@OptIn(ExperimentalWasmJsInterop::class)
actual fun saveBootThemeColor(color: Color) {
    try {
        localStorage.setItem(BOOT_THEME_COLOR_KEY, color.toCssHex())
    } catch (e: JsException) {
        throw BootThemeColorWriteException(e)
    }
}

private class BootThemeColorWriteException(cause: Throwable) : Exception(cause)

private fun Color.toCssHex(): String =
    "#${(toArgb() and RGB_MASK).toString(HEX_RADIX).padStart(CSS_HEX_LENGTH, HEX_PAD_CHARACTER).uppercase()}"

private const val RGB_MASK = 0x00FFFFFF
private const val HEX_RADIX = 16
private const val CSS_HEX_LENGTH = 6
private const val HEX_PAD_CHARACTER = '0'
private const val BOOT_THEME_COLOR_KEY = "theme.boot_color"

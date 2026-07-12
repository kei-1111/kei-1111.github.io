package io.github.kei_1111.core.utils

import kotlinx.browser.window

actual fun openUrl(url: String) {
    window.open(url)
}

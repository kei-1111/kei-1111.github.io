package io.github.kei_1111.utils

import kotlinx.browser.window

actual fun openUrl(url: String) {
    window.open(url)
}

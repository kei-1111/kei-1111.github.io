package org.example.project.utils

import kotlinx.browser.window

actual fun openUrl(url: String) {
    window.open(url)
}

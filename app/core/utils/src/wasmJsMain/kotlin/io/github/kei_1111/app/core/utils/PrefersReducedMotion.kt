package io.github.kei_1111.app.core.utils

actual fun prefersReducedMotion(): Boolean = prefersReducedMotionJs()

private fun prefersReducedMotionJs(): Boolean =
    js("window.matchMedia('(prefers-reduced-motion: reduce)').matches")

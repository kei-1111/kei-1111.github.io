package io.github.kei_1111.app.feature.splash.destination.splash.model

/** Desktop / Mobile が同じ文字列を出すため、版数の更新漏れが起きないよう一箇所で持つ。 */
internal const val SPLASH_APP_NAME = "kei-1111 portfolio"

internal fun splashAppVersion(isDark: Boolean): String =
    "Portfolio IDE 2026.7 (${if (isDark) "Islands Dark" else "Islands Light"})"

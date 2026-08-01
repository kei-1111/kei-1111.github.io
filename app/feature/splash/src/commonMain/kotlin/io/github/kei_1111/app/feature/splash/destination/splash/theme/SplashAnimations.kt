@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.splash.destination.splash.theme

internal data object SplashAnimations {
    /** ページ表示中のみ計測。超過したらビルド失敗としてスプラッシュに留まる */
    const val FontLoadTimeoutMillis = 10_000L

    /** 一瞬で消えるチラつきを防ぐ */
    const val MinDisplayMillis = 400L

    const val SuccessToExitMillis = 250L

    const val ProgressBarCycleMillis = 1_400

    /** トラック幅比 */
    const val ProgressBarStartFraction = -0.35f

    /** トラック幅比 */
    const val ProgressBarEndFraction = 1.05f

    /** トラック幅比 */
    const val ProgressBarMinWidthFraction = 0.30f

    /** トラック幅比 */
    const val ProgressBarMaxWidthFraction = 0.45f
}

@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.splash.destination.splash.theme

internal data object SplashAnimations {
    /** フォントロード待ちのタイムアウト（ページ表示中のみ計測）。超過したらビルド失敗としてスプラッシュに留まる */
    const val FontLoadTimeoutMillis = 10_000L

    /** 一瞬で消えるチラつきを防ぐ最低表示時間 */
    const val MinDisplayMillis = 400L

    /** BUILD SUCCESSFUL 表示から画面遷移までの時間 */
    const val SuccessToExitMillis = 250L

    /** 不確定プログレスバーの1周期 */
    const val ProgressBarCycleMillis = 1_400

    /** バー左端の開始位置（トラック幅比） */
    const val ProgressBarStartFraction = -0.35f

    /** バー左端の終了位置（トラック幅比） */
    const val ProgressBarEndFraction = 1.05f

    /** バー幅の最小値（トラック幅比） */
    const val ProgressBarMinWidthFraction = 0.30f

    /** バー幅の最大値（トラック幅比） */
    const val ProgressBarMaxWidthFraction = 0.45f
}

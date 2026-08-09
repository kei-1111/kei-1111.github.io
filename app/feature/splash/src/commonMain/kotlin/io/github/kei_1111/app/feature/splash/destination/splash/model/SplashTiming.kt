package io.github.kei_1111.app.feature.splash.destination.splash.model

/** SplashViewModel が delay で読む進行制御の時間。視覚アニメーションのトークン（theme/SplashAnimations）とは別物。 */
internal data object SplashTiming {
    /** ページ表示中のみ計測。超過したらビルド失敗としてスプラッシュに留まる */
    const val FontLoadTimeoutMillis = 10_000L

    /** 一瞬で消えるチラつきを防ぐ */
    const val MinDisplayMillis = 400L

    const val SuccessToExitMillis = 250L
}

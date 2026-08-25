package io.github.kei_1111.app.feature.profile.destination.profile.model

/**
 * データの取得状態から決まる表示フェーズ。選択ページのエディタ／Preview だけでなく、
 * Contributions や TODO のように独立して落ちうるセクションも同じ語彙を共有する
 * （別々に導出すると片方だけ直したときに食い違う）。
 */
internal enum class LoadPhase {
    /** 取得待ち。スケルトンやビルド中表示。 */
    Loading,

    /** 取得失敗。アニメーションは止め、再試行導線を出す。 */
    Failed,

    /** データ到着済み。静的コンテンツは常にこれ。 */
    Ready,
}

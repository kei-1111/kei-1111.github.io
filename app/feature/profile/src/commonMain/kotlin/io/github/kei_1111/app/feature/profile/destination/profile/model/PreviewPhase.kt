package io.github.kei_1111.app.feature.profile.destination.profile.model

/**
 * 選択ページの取得状態から決まる表示フェーズ。エディタのスケルトンと Preview のビルド表示は
 * 同じフェーズを見る（別々に導出すると片方だけ直したときに食い違う）。
 */
internal enum class PreviewPhase {
    /** 取得待ち。エディタはスケルトン、Preview はビルド中表示。 */
    Loading,

    /** 取得失敗。スケルトンは静止し、Preview は再試行導線を出す。 */
    Failed,

    /** データ到着済み。ライセンスページは静的コンテンツのため常にこれ。 */
    Ready,
}

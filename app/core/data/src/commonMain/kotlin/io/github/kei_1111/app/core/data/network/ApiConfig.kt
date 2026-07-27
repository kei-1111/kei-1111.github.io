package io.github.kei_1111.app.core.data.network

/** 自作バックエンド API(Cloud Run)。到達不能なら Repository が例外を送出し、ViewModel 側の asResult() が Result.Error に変換する。 */
internal const val API_BASE_URL = "https://kei-1111-server-672756196519.asia-northeast1.run.app"

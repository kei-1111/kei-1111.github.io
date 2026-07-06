package io.github.kei_1111.core.designsystem.theme

import androidx.compose.runtime.Composable

/**
 * 各フォントファミリーを Compose Resources のフォントキャッシュへ実際に読み込み、完了したかを返す。
 *
 * wasm では Font(resource) が非同期ロードのため、キャッシュ未投入のまま画面を描画すると
 * 日本語グリフを持たない既定フォントで確定してしまう。画面遷移前に本 API でロードを完了させる。
 * ロード対象の (resource, weight) の組は各ファミリー定義（[JetBrainsMonoFamily] ほか）と一致させること。
 */
@Composable
expect fun rememberJetBrainsMonoFontsLoaded(): Boolean

@Composable
expect fun rememberNotoSansJpFontsLoaded(): Boolean

@Composable
expect fun rememberZenKakuGothicNewFontsLoaded(): Boolean

package io.github.kei_1111.app.core.navigation

import androidx.compose.ui.window.DialogProperties

/**
 * 背後を暗転させず、幅もプラットフォーム既定に縛られないダイアログ設定。
 *
 * スクリム色を指定する `scrimColor` は wasmJs の [DialogProperties] にしか無いため expect/actual で分ける。
 */
expect fun scrimlessDialogProperties(): DialogProperties

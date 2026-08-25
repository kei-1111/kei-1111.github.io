package io.github.kei_1111.app.core.utils

import androidx.compose.runtime.Composable

/**
 * 実 AS の Search Everywhere と同じダブル Shift を検出して [onDoubleShift] を呼ぶ。
 * Compose のフォーカス所有者に依存させないため、DOM のキーイベントを直接購読する。
 */
@Composable
expect fun DoubleShiftEffect(onDoubleShift: () -> Unit)

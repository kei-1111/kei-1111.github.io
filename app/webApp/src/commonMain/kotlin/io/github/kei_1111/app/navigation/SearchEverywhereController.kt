package io.github.kei_1111.app.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * ダブル Shift（`DoubleShiftListener.kt`）から Search Everywhere を開く要求を [AppNavDisplay] へ渡すブリッジ。
 *
 * MVI の外に置いている理由: 発火元が Composition の外にある `document` の生リスナーで、どの destination の
 * ViewModel にも属さない。「Profile にいるときだけ開く」の判断も画面をまたぐナビゲーションの領分なので、
 * [AppNavDisplay] が `snapshotFlow` でここを購読して back stack を操作する。
 *
 * [openTick] が Boolean ではなく単調増加の Int なのは、閉じ直後の再要求が「同じ値への代入」になって
 * 落ちるのを避けるため。
 */
internal object SearchEverywhereController {
    var openTick: Int by mutableStateOf(0)
        private set

    fun requestOpen() {
        openTick++
    }
}

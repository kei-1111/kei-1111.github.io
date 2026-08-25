package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

/** ドラッグ中に細いハンドル外へポインタが出ても resize カーソルを維持する。 */
internal fun Modifier.resizeCursorOverride(cursor: PointerIcon?): Modifier =
    if (cursor != null) pointerHoverIcon(cursor, overrideDescendants = true) else this

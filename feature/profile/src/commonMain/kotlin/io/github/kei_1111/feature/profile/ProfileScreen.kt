@file:Suppress("MagicNumber")

package io.github.kei_1111.feature.profile

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** Mobile レイアウト（ツリーをオーバーレイ表示）に切り替えるブレークポイント。 */
private val CompactWidth = 900.dp

@Suppress("ModifierMissing")
@Composable
fun ProfileScreen() {
    var selectedPage by remember { mutableStateOf(EditorPage.Profile) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
        if (screenWidth < CompactWidth) {
            ProfileMobileContent(
                selectedPage = selectedPage,
                onSelectPage = { selectedPage = it },
            )
        } else {
            ProfileDesktopContent(
                selectedPage = selectedPage,
                onSelectPage = { selectedPage = it },
            )
        }
    }
}

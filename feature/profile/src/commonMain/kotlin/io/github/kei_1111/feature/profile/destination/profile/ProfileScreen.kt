@file:Suppress("MagicNumber")

package io.github.kei_1111.feature.profile.destination.profile

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kei_1111.core.mvi.MviEffect
import io.github.kei_1111.core.utils.openUrl

/** Mobile レイアウト（ツリーをオーバーレイ表示）に切り替えるブレークポイント。 */
private val CompactWidth = 900.dp

@Composable
internal fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MviEffect(
        effect = state.effect,
        onConsume = { viewModel.onIntent(ProfileIntent.ConsumeEffect) },
    ) { effect ->
        when (effect) {
            is ProfileEffect.OpenUrl -> openUrl(effect.url)
        }
    }

    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
        val layout = if (screenWidth < CompactWidth) ProfileLayout.Mobile else ProfileLayout.Desktop

        LaunchedEffect(layout) {
            onIntent(ProfileIntent.OnLayoutChanged(layout))
        }

        // 初回フレーム（profile 未到着）は何も描画しない。700ms のナビゲーション遷移でクロスフェード
        // されるため、この空白フレームは画面上には現れない。
        if (state.profile != null) {
            when (layout) {
                ProfileLayout.Mobile -> ProfileMobileContent(state = state, onIntent = onIntent)
                ProfileLayout.Desktop -> ProfileDesktopContent(state = state, onIntent = onIntent)
            }
        }
    }
}

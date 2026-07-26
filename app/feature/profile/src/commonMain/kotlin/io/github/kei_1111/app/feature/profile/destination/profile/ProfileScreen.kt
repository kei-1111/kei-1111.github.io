package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.designsystem.layout.windowLayoutFor
import io.github.kei_1111.app.feature.profile.destination.profile.content.ProfileDesktopContent
import io.github.kei_1111.app.feature.profile.destination.profile.content.ProfileMobileContent

@Composable
internal fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
        val layout = windowLayoutFor(screenWidth)

        LaunchedEffect(layout) {
            onIntent(ProfileIntent.UpdateLayout(layout))
        }

        when (layout) {
            WindowLayout.Mobile -> ProfileMobileContent(
                state = state,
                onIntent = onIntent,
                onToggleTheme = onToggleTheme,
            )
            WindowLayout.Desktop -> ProfileDesktopContent(
                state = state,
                onIntent = onIntent,
                onToggleTheme = onToggleTheme,
            )
        }
    }
}

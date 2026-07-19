package io.github.kei_1111.app.feature.splash.destination.splash

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.designsystem.layout.windowLayoutFor
import io.github.kei_1111.app.feature.splash.destination.splash.content.SplashDesktopContent
import io.github.kei_1111.app.feature.splash.destination.splash.content.SplashMobileContent

@Composable
internal fun SplashScreen(
    state: SplashState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
        when (windowLayoutFor(screenWidth)) {
            WindowLayout.Mobile -> SplashMobileContent(
                state = state,
                modifier = Modifier.fillMaxSize(),
            )

            WindowLayout.Desktop -> SplashDesktopContent(
                state = state,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

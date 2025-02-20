package io.github.kei_1111.ui.feature.profile

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.kei_1111.DeviceType
import io.github.kei_1111.MobileWidth

@Suppress("ModifierMissing")
@Composable
fun ProfileScreen() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
        val deviceType =
            if (screenWidth < MobileWidth) DeviceType.Mobile else DeviceType.Desktop
        when (deviceType) {
            DeviceType.Mobile -> {
                ProfileMobileContent()
            }

            DeviceType.Desktop -> {
                ProfileDesktopContent()
            }
        }
    }
}

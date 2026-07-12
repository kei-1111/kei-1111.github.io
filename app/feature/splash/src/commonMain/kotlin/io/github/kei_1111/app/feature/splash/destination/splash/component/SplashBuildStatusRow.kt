@file:Suppress("UnusedPrivateMember")

package io.github.kei_1111.app.feature.splash.destination.splash.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.splash.destination.splash.BuildStatus
import io.github.kei_1111.app.feature.splash.theme.SplashDimensions

/** ./gradlew buildPortfolio と BUILD RUNNING…/BUILD SUCCESSFUL/BUILD FAILED を左右に振り分けるキャプション行。 */
@Composable
internal fun SplashBuildStatusRow(
    buildStatus: BuildStatus,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "./gradlew buildPortfolio",
            fontFamily = KeiTheme.typography.mono.fontFamily,
            fontSize = fontSize,
            color = KeiTheme.colors.splashTextDim,
        )
        Text(
            text = when (buildStatus) {
                BuildStatus.Running -> "BUILD RUNNING…"
                BuildStatus.Success -> "BUILD SUCCESSFUL"
                BuildStatus.Failed -> "BUILD FAILED"
            },
            fontFamily = KeiTheme.typography.mono.fontFamily,
            fontSize = fontSize,
            color = when (buildStatus) {
                BuildStatus.Running -> KeiTheme.colors.splashTextDim
                BuildStatus.Success -> KeiTheme.colors.splashStatusDone
                BuildStatus.Failed -> KeiTheme.colors.splashStatusFailed
            },
        )
    }
}

@Preview
@Composable
private fun SplashBuildStatusRowPreview() {
    KeiTheme {
        SplashBuildStatusRow(
            buildStatus = BuildStatus.Running,
            fontSize = SplashDimensions.CaptionFontSize,
        )
    }
}

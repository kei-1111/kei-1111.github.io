@file:Suppress("UnusedPrivateMember")

package io.github.kei_1111.feature.splash.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import io.github.kei_1111.core.designsystem.theme.AppTheme
import io.github.kei_1111.core.designsystem.theme.JetBrainsMonoFamily
import io.github.kei_1111.feature.splash.BuildStatus
import io.github.kei_1111.feature.splash.theme.SplashColors
import io.github.kei_1111.feature.splash.theme.SplashDimensions

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
            fontFamily = JetBrainsMonoFamily(),
            fontSize = fontSize,
            color = SplashColors.TextDim,
        )
        Text(
            text = when (buildStatus) {
                BuildStatus.Running -> "BUILD RUNNING…"
                BuildStatus.Success -> "BUILD SUCCESSFUL"
                BuildStatus.Failed -> "BUILD FAILED"
            },
            fontFamily = JetBrainsMonoFamily(),
            fontSize = fontSize,
            color = when (buildStatus) {
                BuildStatus.Running -> SplashColors.TextDim
                BuildStatus.Success -> SplashColors.StatusDone
                BuildStatus.Failed -> SplashColors.StatusFailed
            },
        )
    }
}

@Preview
@Composable
private fun SplashBuildStatusRowPreview() {
    AppTheme(darkTheme = true) {
        SplashBuildStatusRow(
            buildStatus = BuildStatus.Running,
            fontSize = SplashDimensions.CaptionFontSize,
        )
    }
}

@file:Suppress("MagicNumber", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.splash.destination.splash.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.ProfileIconImage
import io.github.kei_1111.app.feature.splash.destination.splash.SplashState
import io.github.kei_1111.app.feature.splash.destination.splash.component.SplashBuildLog
import io.github.kei_1111.app.feature.splash.destination.splash.component.SplashBuildStatusRow
import io.github.kei_1111.app.feature.splash.destination.splash.component.SplashProgressBar
import io.github.kei_1111.app.feature.splash.destination.splash.model.BuildStatus
import io.github.kei_1111.app.feature.splash.destination.splash.model.SplashStep
import io.github.kei_1111.app.feature.splash.destination.splash.theme.SplashDimensions
import org.jetbrains.compose.resources.painterResource

/** デスクトップ用スプラッシュ。デスク中央に Android Studio 起動画面風のカードを1枚置く。 */
@Composable
internal fun SplashDesktopContent(
    state: SplashState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(KeiTheme.colors.splashDesk),
        contentAlignment = Alignment.Center,
    ) {
        SplashCard(
            jetBrainsMonoStep = state.jetBrainsMonoStep,
            notoSansJpStep = state.notoSansJpStep,
            zenKakuGothicNewStep = state.zenKakuGothicNewStep,
            renderStep = state.renderStep,
            buildStatus = state.buildStatus,
            modifier = Modifier
                .padding(horizontal = SplashDimensions.ScreenPadding)
                .widthIn(max = SplashDimensions.CardWidth),
        )
    }
}

@Composable
private fun SplashCard(
    jetBrainsMonoStep: SplashStep,
    notoSansJpStep: SplashStep,
    zenKakuGothicNewStep: SplashStep,
    renderStep: SplashStep,
    buildStatus: BuildStatus,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(SplashDimensions.CardCornerRadius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(KeiTheme.colors.splashCard)
            .border(
                width = SplashDimensions.CardBorderWidth,
                color = KeiTheme.colors.splashCardBorder,
                shape = cardShape,
            )
            .padding(
                horizontal = SplashDimensions.CardPaddingHorizontal,
                vertical = SplashDimensions.CardPaddingVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(SplashDimensions.CardGap),
    ) {
        SplashHeader()
        SplashBuildLog(
            jetBrainsMonoStep = jetBrainsMonoStep,
            notoSansJpStep = notoSansJpStep,
            zenKakuGothicNewStep = zenKakuGothicNewStep,
            renderStep = renderStep,
            fontSize = SplashDimensions.LogFontSize,
            lineHeight = SplashDimensions.LogLineHeight,
        )
        SplashProgress(
            buildStatus = buildStatus,
        )
    }
}

@Composable
private fun SplashHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SplashDimensions.HeaderGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SplashAppIcon()
        SplashAppInfo()
    }
}

@Composable
private fun SplashAppIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(ProfileIconImage),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(SplashDimensions.IconSize)
            .clip(RoundedCornerShape(SplashDimensions.IconCornerRadius)),
    )
}

@Composable
private fun SplashAppInfo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SplashDimensions.TitleGap),
    ) {
        SplashAppName()
        SplashAppVersion()
    }
}

@Composable
private fun SplashAppName(modifier: Modifier = Modifier) {
    Text(
        text = "kei-1111 portfolio",
        modifier = modifier,
        fontFamily = KeiTheme.typography.mono.fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = SplashDimensions.TitleFontSize,
        color = KeiTheme.colors.splashTextTitle,
    )
}

@Composable
private fun SplashAppVersion(modifier: Modifier = Modifier) {
    Text(
        text = "Portfolio IDE 2026.7 (Islands Dark)",
        modifier = modifier,
        fontFamily = KeiTheme.typography.mono.fontFamily,
        fontSize = SplashDimensions.VersionFontSize,
        color = KeiTheme.colors.splashTextDim,
    )
}

@Composable
private fun SplashProgress(
    buildStatus: BuildStatus,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SplashDimensions.ProgressGap),
    ) {
        SplashProgressBar(
            isBuildFailed = buildStatus == BuildStatus.Failed,
            modifier = Modifier.fillMaxWidth(),
        )
        SplashBuildStatusRow(
            buildStatus = buildStatus,
            fontSize = SplashDimensions.CaptionFontSize,
        )
    }
}

@Preview
@Composable
private fun SplashDesktopContentPreview() {
    KeiTheme {
        SplashDesktopContent(
            state = SplashState(
                jetBrainsMonoStep = SplashStep.Done,
                notoSansJpStep = SplashStep.Done,
                zenKakuGothicNewStep = SplashStep.Running,
                renderStep = SplashStep.Running,
                buildStatus = BuildStatus.Running,
            ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

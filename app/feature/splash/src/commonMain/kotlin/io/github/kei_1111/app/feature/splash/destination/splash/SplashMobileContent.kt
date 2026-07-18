@file:Suppress("UnusedPrivateMember")

package io.github.kei_1111.app.feature.splash.destination.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import io.github.kei_1111.app.feature.splash.destination.splash.component.SplashBuildLog
import io.github.kei_1111.app.feature.splash.destination.splash.component.SplashBuildStatusRow
import io.github.kei_1111.app.feature.splash.destination.splash.component.SplashProgressBar
import io.github.kei_1111.app.feature.splash.theme.SplashDimensions
import org.jetbrains.compose.resources.painterResource

/**
 * モバイル用スプラッシュ。カードを使わず画面全体を使う
 * フルブリード型(ネイティブアプリの起動画面に近い構成)。
 * 中央にアイコン・タイトル・進捗バー、下端にビルドログとフッターを置く。
 */
@Composable
internal fun SplashMobileContent(
    state: SplashState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(KeiTheme.colors.splashDesk)
            .padding(
                vertical = SplashDimensions.MobilePaddingVertical,
                horizontal = SplashDimensions.MobilePaddingHorizontal,
            ),
    ) {
        SplashMobileHero(
            buildStatus = state.buildStatus,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        SplashMobileFooter(
            jetBrainsMonoStep = state.jetBrainsMonoStep,
            notoSansJpStep = state.notoSansJpStep,
            zenKakuGothicNewStep = state.zenKakuGothicNewStep,
            renderStep = state.renderStep,
            buildStatus = state.buildStatus,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** 上下中央に置くアイコン・サイト名・バージョン・進捗バーのブロック。 */
@Composable
private fun SplashMobileHero(
    buildStatus: BuildStatus,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = SplashDimensions.MobileCenterGap,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        SplashAppIcon()
        SplashAppName()
        SplashAppVersion()
        SplashProgressBar(
            isBuildFailed = buildStatus == BuildStatus.Failed,
            modifier = Modifier
                .padding(top = SplashDimensions.MobileProgressTopMargin)
                .width(SplashDimensions.MobileProgressBarWidth),
        )
    }
}

@Composable
private fun SplashAppIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(ProfileIconImage),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(SplashDimensions.MobileIconSize)
            .clip(RoundedCornerShape(SplashDimensions.MobileIconCornerRadius)),
    )
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
        fontSize = SplashDimensions.MobileVersionFontSize,
        color = KeiTheme.colors.splashTextDim,
    )
}

/** 画面下端に置くビルドログとキャプションのブロック。 */
@Composable
private fun SplashMobileFooter(
    jetBrainsMonoStep: SplashStep,
    notoSansJpStep: SplashStep,
    zenKakuGothicNewStep: SplashStep,
    renderStep: SplashStep,
    buildStatus: BuildStatus,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        SplashBuildLog(
            jetBrainsMonoStep = jetBrainsMonoStep,
            notoSansJpStep = notoSansJpStep,
            zenKakuGothicNewStep = zenKakuGothicNewStep,
            renderStep = renderStep,
            fontSize = SplashDimensions.MobileLogFontSize,
            lineHeight = SplashDimensions.MobileLogLineHeight,
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = SplashDimensions.MobileLogFooterGap),
            thickness = SplashDimensions.MobileFooterBorderWidth,
            color = KeiTheme.colors.splashCardBorder,
        )
        SplashBuildStatusRow(
            buildStatus = buildStatus,
            fontSize = SplashDimensions.MobileFooterFontSize,
            modifier = Modifier.padding(top = SplashDimensions.MobileFooterPaddingTop),
        )
    }
}

@Preview
@Composable
private fun SplashMobileContentPreview() {
    KeiTheme {
        SplashMobileContent(
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

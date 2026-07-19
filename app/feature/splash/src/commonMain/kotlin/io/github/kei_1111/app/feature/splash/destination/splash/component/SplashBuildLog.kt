@file:Suppress("MagicNumber", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.splash.destination.splash.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.splash.destination.splash.model.SplashStep
import io.github.kei_1111.app.feature.splash.theme.SplashDimensions

/** フォントロード・レンダリングの進行を Gradle ビルドログ風に表示する。 */
@Composable
internal fun SplashBuildLog(
    jetBrainsMonoStep: SplashStep,
    notoSansJpStep: SplashStep,
    zenKakuGothicNewStep: SplashStep,
    renderStep: SplashStep,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SplashDimensions.LogGap),
    ) {
        SplashLogRow(
            step = jetBrainsMonoStep,
            label = "Loading JetBrains Mono…",
            fontSize = fontSize,
            lineHeight = lineHeight,
        )
        SplashLogRow(
            step = notoSansJpStep,
            label = "Loading Noto Sans JP…",
            fontSize = fontSize,
            lineHeight = lineHeight,
        )
        SplashLogRow(
            step = zenKakuGothicNewStep,
            label = "Loading Zen Kaku Gothic New…",
            fontSize = fontSize,
            lineHeight = lineHeight,
        )
        SplashLogRow(
            step = renderStep,
            label = "Rendering ProfilePreview…",
            fontSize = fontSize,
            lineHeight = lineHeight,
        )
    }
}

@Composable
private fun SplashLogRow(
    step: SplashStep,
    label: String,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SplashDimensions.LogMarkGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SplashStepMark(
            step = step,
        )
        Text(
            text = label,
            fontFamily = KeiTheme.typography.mono.fontFamily,
            fontSize = fontSize,
            lineHeight = lineHeight,
            color = KeiTheme.colors.splashTextLog,
        )
    }
}

/** 進行中は黄色の円弧（⟳ 相当）、完了で緑のチェックマーク（✓ 相当）、失敗で赤のバツ印（✗ 相当）を描く。 */
@Composable
private fun SplashStepMark(
    step: SplashStep,
    modifier: Modifier = Modifier,
) {
    val color = when (step) {
        SplashStep.Running -> KeiTheme.colors.splashStatusRunning
        SplashStep.Done -> KeiTheme.colors.splashStatusDone
        SplashStep.Failed -> KeiTheme.colors.splashStatusFailed
    }

    Canvas(
        modifier = modifier.size(SplashDimensions.MarkSize),
    ) {
        val strokeWidth = SplashDimensions.MarkStrokeWidth.toPx()
        when (step) {
            SplashStep.Running -> drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            SplashStep.Done -> {
                val checkPath = Path().apply {
                    moveTo(size.width * 0.1f, size.height * 0.55f)
                    lineTo(size.width * 0.4f, size.height * 0.85f)
                    lineTo(size.width * 0.9f, size.height * 0.2f)
                }
                drawPath(
                    path = checkPath,
                    color = color,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }

            SplashStep.Failed -> {
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.15f, size.height * 0.15f),
                    end = Offset(size.width * 0.85f, size.height * 0.85f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.85f, size.height * 0.15f),
                    end = Offset(size.width * 0.15f, size.height * 0.85f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SplashBuildLogPreview() {
    KeiTheme {
        SplashBuildLog(
            jetBrainsMonoStep = SplashStep.Done,
            notoSansJpStep = SplashStep.Done,
            zenKakuGothicNewStep = SplashStep.Running,
            renderStep = SplashStep.Running,
            fontSize = SplashDimensions.LogFontSize,
            lineHeight = SplashDimensions.LogLineHeight,
        )
    }
}

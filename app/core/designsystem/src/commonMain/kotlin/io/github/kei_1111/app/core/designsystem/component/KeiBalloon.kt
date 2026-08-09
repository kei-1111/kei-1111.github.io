@file:Suppress("MagicNumber", "TooManyFunctions")

package io.github.kei_1111.app.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.ThemedIcon
import io.github.kei_1111.app.core.utils.prefersReducedMotion
import kotlinx.coroutines.delay

/** バルーン通知の重大度。実 AS 同様、先頭アイコンだけが変わり面の色は共通。 */
enum class KeiBalloonSeverity { Info, Warning }

data object KeiBalloonDefaults {
    /** 実 AS のバルーン幅に合わせた固定幅。本文を 2 行で打ち切るため高さだけが可変。 */
    val Width = 360.dp
    const val AutoDismissMillis = 10_000L
    val StackSpacing = 8.dp
    const val TransitionMillis = 220
}

/**
 * 右下に浮く Android Studio 風のバルーン通知。表示後しばらくで自動的に消え、ホバー中は消えない。
 * タイムアウトと閉じるボタンのどちらも、退出アニメーションを終えてから [onDismiss] を呼ぶ。
 */
@Composable
fun KeiBalloon(
    severity: KeiBalloonSeverity,
    title: String,
    message: String,
    closeContentDescription: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    autoDismissMillis: Long = KeiBalloonDefaults.AutoDismissMillis,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val reducedMotion = prefersReducedMotion()

    BalloonAutoDismiss(hovered = hovered, autoDismissMillis = autoDismissMillis) {
        visibleState.targetState = false
    }

    // 退出アニメーションを終えてから呼び出し側の削除を走らせる。初回フレームは targetState が
    // 既に true で isIdle が false のため、ここは通らない。
    LaunchedEffect(visibleState.isIdle, visibleState.currentState) {
        if (visibleState.isIdle && !visibleState.currentState) currentOnDismiss()
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = if (reducedMotion) {
            EnterTransition.None
        } else {
            fadeIn(tween(KeiBalloonDefaults.TransitionMillis)) +
                slideInHorizontally(tween(KeiBalloonDefaults.TransitionMillis)) { it / 4 }
        },
        exit = if (reducedMotion) {
            ExitTransition.None
        } else {
            fadeOut(tween(KeiBalloonDefaults.TransitionMillis)) +
                slideOutHorizontally(tween(KeiBalloonDefaults.TransitionMillis)) { it / 4 }
        },
    ) {
        Box(
            modifier = modifier
                .width(KeiBalloonDefaults.Width)
                .shadow(elevation = 6.dp, shape = KeiTheme.shapes.card)
                .clip(KeiTheme.shapes.card)
                .background(KeiTheme.colors.notification)
                .border(1.dp, KeiTheme.colors.notificationBorder, KeiTheme.shapes.card)
                .hoverable(interactionSource),
        ) {
            BalloonContentRow(severity = severity, title = title, message = message, actions = actions)
            BalloonCloseButton(
                contentDescription = closeContentDescription,
                onClick = { visibleState.targetState = false },
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

/** 縦積みの器。バルーンの実寸だけを占有し、背後のコンテンツのクリックを塞がない。 */
@Composable
fun KeiBalloonHost(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(KeiBalloonDefaults.StackSpacing),
        horizontalAlignment = Alignment.End,
        content = content,
    )
}

/** バルーン下部のアクションリンク。実 AS 同様、下線なしのリンク色テキスト。 */
@Composable
fun KeiBalloonActionLink(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.link),
        modifier = modifier.clickable(onClick = onClick),
    )
}

/** ホバー中はタイマーを止め、外れたら測り直す（実 AS のバルーンと同じ振る舞い）。 */
@Composable
private fun BalloonAutoDismiss(hovered: Boolean, autoDismissMillis: Long, onTimeout: () -> Unit) {
    val currentOnTimeout by rememberUpdatedState(onTimeout)
    LaunchedEffect(hovered) {
        if (hovered) return@LaunchedEffect
        delay(autoDismissMillis)
        currentOnTimeout()
    }
}

@Composable
private fun BalloonContentRow(
    severity: KeiBalloonSeverity,
    title: String,
    message: String,
    actions: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 28.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BalloonSeverityIcon(severity)
        BalloonTextColumn(title = title, message = message, actions = actions)
    }
}

@Composable
private fun BalloonSeverityIcon(severity: KeiBalloonSeverity) {
    val icon: ThemedIcon = when (severity) {
        KeiBalloonSeverity.Info -> KeiTheme.icons.info
        KeiBalloonSeverity.Warning -> KeiTheme.icons.warning
    }
    KeiIcon(
        icon = icon,
        contentDescription = null,
        modifier = Modifier.padding(top = 1.dp).size(16.dp),
    )
}

@Composable
private fun BalloonTextColumn(
    title: String,
    message: String,
    actions: @Composable RowScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = KeiTheme.typography.chrome.copy(
                fontWeight = FontWeight.Bold,
                color = KeiTheme.colors.textPrimary,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = message,
            style = KeiTheme.typography.chrome.copy(lineHeight = 17.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            content = actions,
        )
    }
}

@Composable
private fun BalloonCloseButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(6.dp)
            .clip(KeiTheme.shapes.chip)
            .clickable(onClick = onClick)
            .padding(2.dp),
    ) {
        KeiIcon(
            icon = KeiTheme.icons.closeSmall,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Preview
@Composable
private fun KeiBalloonPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.desk).padding(16.dp)) {
            KeiBalloon(
                severity = KeiBalloonSeverity.Info,
                title = "Migrate to Gradle Daemon toolchain",
                message = "Projects using Daemon toolchain allow builds to automatically detect installed " +
                    "toolchains given the configured criteria.",
                closeContentDescription = "Dismiss",
                onDismiss = {},
                actions = {
                    KeiBalloonActionLink(label = "Learn more", onClick = {})
                    KeiBalloonActionLink(label = "Migrate", onClick = {})
                    KeiBalloonActionLink(label = "Ignore", onClick = {})
                },
            )
        }
    }
}

@Preview
@Composable
private fun KeiBalloonWarningPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.desk).padding(16.dp)) {
            KeiBalloon(
                severity = KeiBalloonSeverity.Warning,
                title = "GitHub sync failed",
                message = "Showing cached content. Some data may be out of date.",
                closeContentDescription = "Dismiss",
                onDismiss = {},
            )
        }
    }
}

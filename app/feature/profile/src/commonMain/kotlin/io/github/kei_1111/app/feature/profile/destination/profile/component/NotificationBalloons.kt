package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.component.KeiBalloon
import io.github.kei_1111.app.core.designsystem.component.KeiBalloonActionLink
import io.github.kei_1111.app.core.designsystem.component.KeiBalloonHost
import io.github.kei_1111.app.core.designsystem.component.KeiBalloonSeverity
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.model.ProfileBalloon
import io.github.kei_1111.test.tags.TestTags
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.notification_close
import kei_1111.app.feature.profile.generated.resources.notification_fallback_message
import kei_1111.app.feature.profile.generated.resources.notification_fallback_title
import kei_1111.app.feature.profile.generated.resources.notification_update_action
import kei_1111.app.feature.profile.generated.resources.notification_update_message
import kei_1111.app.feature.profile.generated.resources.notification_update_message_first_visit
import kei_1111.app.feature.profile.generated.resources.notification_update_message_one
import kei_1111.app.feature.profile.generated.resources.notification_update_title
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NotificationBalloons(
    balloons: ImmutableList<ProfileBalloon>,
    onClickOpenChangelog: () -> Unit,
    onDismissBalloon: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeiBalloonHost(modifier = modifier) {
        balloons.forEach { balloon ->
            key(balloon.id) {
                when (balloon) {
                    is ProfileBalloon.SiteUpdated -> SiteUpdatedBalloon(
                        newPullRequestCount = balloon.newPullRequestCount,
                        onClickOpenChangelog = onClickOpenChangelog,
                        onDismiss = { onDismissBalloon(balloon.id) },
                    )

                    is ProfileBalloon.FallbackWarning -> FallbackWarningBalloon(
                        onDismiss = { onDismissBalloon(balloon.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SiteUpdatedBalloon(
    newPullRequestCount: Int?,
    onClickOpenChangelog: () -> Unit,
    onDismiss: () -> Unit,
) {
    KeiBalloon(
        severity = KeiBalloonSeverity.Info,
        title = stringResource(Res.string.notification_update_title),
        message = when (newPullRequestCount) {
            null -> stringResource(Res.string.notification_update_message_first_visit)
            1 -> stringResource(Res.string.notification_update_message_one)
            else -> stringResource(Res.string.notification_update_message, newPullRequestCount)
        },
        closeContentDescription = stringResource(Res.string.notification_close),
        onDismiss = onDismiss,
        modifier = Modifier.testTag(TestTags.Profile.NOTIFICATION_BALLOON_SITE_UPDATED),
        actions = {
            KeiBalloonActionLink(
                label = stringResource(Res.string.notification_update_action),
                onClick = onClickOpenChangelog,
                modifier = Modifier.testTag(TestTags.Profile.NOTIFICATION_BALLOON_SITE_UPDATED_ACTION),
            )
        },
    )
}

@Composable
private fun FallbackWarningBalloon(onDismiss: () -> Unit) {
    KeiBalloon(
        severity = KeiBalloonSeverity.Warning,
        title = stringResource(Res.string.notification_fallback_title),
        message = stringResource(Res.string.notification_fallback_message),
        closeContentDescription = stringResource(Res.string.notification_close),
        onDismiss = onDismiss,
        modifier = Modifier.testTag(TestTags.Profile.NOTIFICATION_BALLOON_FALLBACK_WARNING),
    )
}

@Preview
@Composable
private fun NotificationBalloonsPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.desk).padding(16.dp)) {
            NotificationBalloons(
                balloons = persistentListOf(
                    ProfileBalloon.SiteUpdated(newPullRequestCount = 5),
                    ProfileBalloon.FallbackWarning,
                ),
                onClickOpenChangelog = {},
                onDismissBalloon = {},
            )
        }
    }
}

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme

/** GitHub / License / Works のカード群が共有する見出しラベル（8px・letter-spacing 0.14em・mono）。 */
@Composable
internal fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.textSecondary).copy(letterSpacing = 1.1.sp),
    )
}

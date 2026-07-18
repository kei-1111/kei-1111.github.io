@file:Suppress("MagicNumber", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component.githubcard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewContributionCalendar
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.ContributionDay
import kotlin.math.roundToInt

private const val DAYS_PER_WEEK = 7
private const val DEFAULT_WEEKS = 53
private const val MAX_LEVEL = 4

/** GitHub 実物と同じ 週×7日 のコントリビューショングリッド＋凡例。 */
@Composable
internal fun ContributionsSection(
    calendar: ContributionCalendar?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        SectionLabel(text = "CONTRIBUTIONS — LAST YEAR")
        ContributionGrid(days = calendar?.days.orEmpty())
        ContributionFooter(calendar = calendar)
    }
}

@Composable
private fun ContributionFooter(
    calendar: ContributionCalendar?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        calendar?.let { ContributionCount(total = it.totalLastYear) }
        Spacer(modifier = Modifier.weight(1f))
        LegendRow()
    }
}

@Composable
private fun ContributionCount(
    total: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = "${total.withThousandsSeparator()} contributions in the last year",
        style = KeiTheme.typography.chrome.copy(fontSize = 7.sp, color = KeiTheme.colors.textSecondary),
    )
}

private fun Int.withThousandsSeparator(): String =
    toString().reversed().chunked(3).joinToString(",").reversed()

@Composable
private fun ContributionGrid(
    days: List<ContributionDay>,
    modifier: Modifier = Modifier,
) {
    var hoveredIndex by remember(days) { mutableStateOf(-1) }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val weeks = if (days.isEmpty()) {
            DEFAULT_WEEKS
        } else {
            (days.size + DAYS_PER_WEEK - 1) / DAYS_PER_WEEK
        }
        val step = maxWidth / weeks
        val density = LocalDensity.current
        val stepPx = with(density) { step.toPx() }
        ContributionCells(
            days = days,
            stepPx = stepPx,
            height = step * DAYS_PER_WEEK,
            onChangeHoveredIndex = { hoveredIndex = it },
        )
        days.getOrNull(hoveredIndex)?.let { day ->
            CellTooltip(day = day, hoveredIndex = hoveredIndex, stepPx = stepPx)
        }
    }
}

@Composable
private fun ContributionCells(
    days: List<ContributionDay>,
    stepPx: Float,
    height: Dp,
    onChangeHoveredIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val levelColors = KeiTheme.colors.contributionLevels
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(days, stepPx) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Move, PointerEventType.Enter -> {
                                val position = event.changes.first().position
                                val col = (position.x / stepPx).toInt()
                                val row = (position.y / stepPx).toInt()
                                val index = col * DAYS_PER_WEEK + row
                                onChangeHoveredIndex(
                                    if (row in 0 until DAYS_PER_WEEK && index in days.indices) index else -1,
                                )
                            }

                            PointerEventType.Exit -> onChangeHoveredIndex(-1)
                        }
                    }
                }
            },
    ) {
        val cellSize = stepPx * 0.72f
        val cornerRadius = CornerRadius(cellSize * 0.3f)
        val total = if (days.isEmpty()) DEFAULT_WEEKS * DAYS_PER_WEEK else days.size
        for (index in 0 until total) {
            val level = days.getOrNull(index)?.level?.coerceIn(0, MAX_LEVEL) ?: 0
            drawRoundRect(
                color = levelColors[level],
                topLeft = Offset(
                    x = index / DAYS_PER_WEEK * stepPx,
                    y = index % DAYS_PER_WEEK * stepPx,
                ),
                size = Size(cellSize, cellSize),
                cornerRadius = cornerRadius,
            )
        }
    }
}

/** セルホバーで `N contributions on {date}` を表示するツールチップ。 */
@Composable
private fun CellTooltip(
    day: ContributionDay,
    hoveredIndex: Int,
    stepPx: Float,
) {
    val density = LocalDensity.current
    val gapPx = with(density) { 4.dp.toPx() }
    Popup(
        offset = IntOffset(
            x = (hoveredIndex / DAYS_PER_WEEK * stepPx).roundToInt(),
            y = ((hoveredIndex % DAYS_PER_WEEK + 1) * stepPx + gapPx).roundToInt(),
        ),
    ) {
        Box(
            modifier = Modifier
                .clip(KeiTheme.shapes.chip)
                .background(KeiTheme.colors.selectionPill)
                .padding(horizontal = 6.dp, vertical = 3.dp),
        ) {
            Text(
                text = "${day.count} contributions on ${day.date}",
                style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.textPrimary),
            )
        }
    }
}

@Composable
private fun LegendRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Less",
            style = KeiTheme.typography.chrome.copy(fontSize = 7.sp, color = KeiTheme.colors.mutedHigh),
        )
        KeiTheme.colors.contributionLevels.forEach { color ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color),
            )
        }
        Text(
            text = "More",
            style = KeiTheme.typography.chrome.copy(fontSize = 7.sp, color = KeiTheme.colors.mutedHigh),
        )
    }
}

@Preview
@Composable
private fun ContributionsSectionPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .background(KeiTheme.colors.cardBackground)
                .padding(20.dp),
        ) {
            ContributionsSection(calendar = PreviewContributionCalendar)
        }
    }
}

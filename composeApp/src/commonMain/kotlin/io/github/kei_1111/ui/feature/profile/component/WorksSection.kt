package io.github.kei_1111.ui.feature.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.lerp
import io.github.kei_1111.core.data.WorkSet
import io.github.kei_1111.core.designsystem.component.BodyMediumText
import io.github.kei_1111.core.designsystem.component.ElevatedButton
import io.github.kei_1111.core.designsystem.component.IconText
import io.github.kei_1111.core.designsystem.component.LabelMediumText
import io.github.kei_1111.core.designsystem.component.TitleSmallText
import io.github.kei_1111.core.designsystem.theme.dimensions.Alpha
import io.github.kei_1111.core.designsystem.theme.dimensions.IconSizes
import io.github.kei_1111.core.designsystem.theme.dimensions.Paddings
import io.github.kei_1111.core.model.DevelopmentType
import io.github.kei_1111.core.model.Work
import io.github.kei_1111.core.utils.openUrl
import io.github.kei_1111.ui.feature.profile.theme.ProfileDimensions
import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.img_github
import kei_1111.composeapp.generated.resources.img_google_play
import org.jetbrains.compose.resources.painterResource
import kotlin.math.absoluteValue

@Composable
fun WorksSection(
    modifier: Modifier = Modifier,
) {
    val worksSize = WorkSet.works.size
    val initialIndex = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { Int.MAX_VALUE },
    )
    val currentWork = WorkSet.works[pagerState.currentPage % worksSize]

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Paddings.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleSmallText(
            text = "Works",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Paddings.Content),
        )
        WorkImages(
            worksSize = worksSize,
            pagerState = pagerState,
            currentWork = currentWork,
        )
        WorkDescription(
            currentWork = currentWork,
        )
        WorkUrls(
            currentWork = currentWork,
        )
    }
}

@Composable
private fun WorkImages(
    worksSize: Int,
    pagerState: PagerState,
    currentWork: Work,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        pageSize = PageSize.Fixed(ProfileDimensions.ImageWidth),
        snapPosition = SnapPosition.Center,
    ) { page ->
        val currentPage = page % worksSize
        val pageOffset =
            ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

        Card(
            modifier = Modifier
                .width(ProfileDimensions.ImageWidth)
                .clip(MaterialTheme.shapes.medium)
                .graphicsLayer {
                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f),
                    )
                }
                .scale(
                    lerp(
                        start = 0.9f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f),
                    ),
                ),
        ) {
            Image(
                painter = painterResource(WorkSet.works[currentPage].image),
                contentDescription = currentWork.name,
            )
        }
    }
}

@Composable
private fun WorkDescription(
    currentWork: Work,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Paddings.Content),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(Paddings.Small),
        ) {
            BodyMediumText(
                text = currentWork.name,
                fontWeight = FontWeight.SemiBold,
            )
            when (currentWork.developmentType) {
                DevelopmentType.Individual -> {
                    IconText(
                        icon = Icons.Default.Person,
                        text = "個人開発",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.Medium),
                    )
                }

                DevelopmentType.Team -> {
                    IconText(
                        icon = Icons.Default.Groups,
                        text = "チーム開発",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.Medium),
                    )
                }
            }
        }
        BodyMediumText(
            text = currentWork.description,
            minLines = ProfileDimensions.WorkSectionDescriptionMinLines,
            maxLines = if (expanded) Int.MAX_VALUE else ProfileDimensions.WorkSectionDescriptionMinLines,
            overflow = TextOverflow.Ellipsis,
        )
        LabelMediumText(
            text = if (expanded) "閉じる" else "詳細を見る",
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = Paddings.ExtraSmall)
                .clickable { expanded = !expanded },
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.Medium),
        )
    }
}

@Composable
private fun WorkUrls(
    currentWork: Work,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IconSizes.Small)
            .padding(horizontal = Paddings.Content),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Paddings.Small, Alignment.End),
    ) {
        currentWork.slideUrl?.let {
            SlideButton(
                onClick = {
                    openUrl(it)
                },
            )
        }
        currentWork.movieUrl?.let {
            MovieButton(
                onClick = {
                    openUrl(it)
                },
            )
        }
        currentWork.githubUrl?.let {
            GitHubButton(
                onClick = {
                    openUrl(it)
                },
            )
        }
        currentWork.googlePlayUrl?.let {
            GooglePlayButton(
                onClick = {
                    openUrl(it)
                },
            )
        }
    }
}

@Composable
private fun SlideButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier
            .size(IconSizes.Small),
        content = {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Slide Button",
            )
        },
    )
}

@Composable
private fun MovieButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier
            .size(IconSizes.Small),
        content = {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = "Movie Button",
            )
        },
    )
}

@Composable
private fun GitHubButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier
            .size(IconSizes.Small),
        content = {
            Image(
                painter = painterResource(Res.drawable.img_github),
                contentDescription = "GitHub Button",
            )
        },
    )
}

@Composable
private fun GooglePlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier
            .size(IconSizes.Small),
        content = {
            Image(
                painter = painterResource(Res.drawable.img_google_play),
                contentDescription = "Google Play Button",
            )
        },
    )
}

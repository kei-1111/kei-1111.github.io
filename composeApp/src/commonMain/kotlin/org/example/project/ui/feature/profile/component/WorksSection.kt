package org.example.project.ui.feature.profile.component

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.lerp
import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.img_github
import kei_1111.composeapp.generated.resources.img_google_play
import org.example.project.data.WorkSet
import org.example.project.model.DevelopmentType
import org.example.project.model.Work
import org.example.project.ui.component.BodyMediumText
import org.example.project.ui.component.ElevatedButton
import org.example.project.ui.component.IconText
import org.example.project.ui.component.TitleSmallText
import org.example.project.ui.feature.profile.theme.ProfileDimensions
import org.example.project.ui.theme.dimensions.Alpha
import org.example.project.ui.theme.dimensions.IconSizes
import org.example.project.ui.theme.dimensions.Paddings
import org.example.project.utils.openUrl
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
            minLines = 5,
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
                    openUrl(currentWork.slideUrl)
                },
            )
        }
        currentWork.movieUrl?.let {
            MovieButton(
                onClick = {
                    openUrl(currentWork.movieUrl)
                },
            )
        }
        currentWork.githubUrl?.let {
            GitHubButton(
                onClick = {
                    openUrl(currentWork.githubUrl)
                },
            )
        }
        currentWork.googlePlayUrl?.let {
            GooglePlayButton(
                onClick = {
                    openUrl(currentWork.googlePlayUrl)
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

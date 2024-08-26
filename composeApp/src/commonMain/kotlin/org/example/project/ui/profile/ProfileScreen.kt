package org.example.project.ui.profile

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.img_profile_icon
import org.example.project.DeviceType
import org.example.project.model.SkillSet
import org.example.project.model.Tool
import org.example.project.model.ToolSet
import org.example.project.model.UiDimensions
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfileScreen() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
        val deviceType = if (screenWidth < 1220.dp) DeviceType.Mobile else DeviceType.Desktop
        ProfileContent(deviceType)
    }
}

@Composable
fun ProfileContent(deviceType: DeviceType) {
    when (deviceType) {
        DeviceType.Mobile -> ProfileMobileContent()
        DeviceType.Desktop -> ProfileDesktopContent()
    }
}

@Composable
fun ProfileMobileContent() {
    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(UiDimensions.contentPadding)
        ) {
            // TODO: Implement mobile content
            Text(
                text = "Mobile Content",
            )
        }
    }
}

@Composable
fun ProfileDesktopContent() {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val animatedSize by animateDpAsState(
        targetValue = if (isHovered) UiDimensions.extraLargeIconSize else UiDimensions.largeIconSize,
        animationSpec = tween(durationMillis = 300)
    )

    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(UiDimensions.contentPadding)
            ) {
                ProfileHeader(
                    modifier = Modifier.fillMaxWidth(),
                    profileIcon = Res.drawable.img_profile_icon,
                    name = "けい"
                )

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(
                            start = UiDimensions.contentPadding,
                            top = UiDimensions.mediumPadding,
                            end = UiDimensions.contentPadding
                        )
                ) {
                    CareerSection(
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        modifier = Modifier.weight(1.5f)
                    ) {
                        SkillsSection()
                        Spacer(modifier = Modifier.weight(1f))
                        ToolsSection()
                    }
                }
            }

            WorksIcon(
                modifier = Modifier.align(Alignment.BottomEnd),
                animatedSize = animatedSize,
                circleColor = MaterialTheme.colorScheme.inversePrimary,
                interactionSource = interactionSource
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        SectionTitle(
            title = "スキル"
        )

        SectionContent(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                SkillSet.ratedSkills.forEach { skill ->
                    RatedSkill(
                        modifier = Modifier.fillMaxWidth(),
                        skillIcon = skill.image,
                        rate = skill.rating
                    )
                }
                SectionSubTitle(
                    title = "使用したことのあるライブラリ"
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    SkillSet.usedLibraries.forEach { libraryName ->
                        UsedLibrary(
                            libraryName = libraryName
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UsedLibrary(
    modifier: Modifier = Modifier, libraryName: String
) {
    Box(
        modifier = modifier.background(
            MaterialTheme.colorScheme.surfaceContainerLowest,
            CircleShape
        ).border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            .padding(
                horizontal = UiDimensions.smallPadding,
                vertical = UiDimensions.extraSmallPadding
            ), contentAlignment = Alignment.Center
    ) {
        BodyText(
            text = libraryName
        )
    }
}

@Composable
fun RatedSkill(
    modifier: Modifier = Modifier, skillIcon: DrawableResource, rate: Int
) {
    val infiniteTransition = rememberInfiniteTransition()

    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically
    ) {
        SkillIcon(
            skillIcon = skillIcon
        )
        Spacer(modifier = Modifier.padding(UiDimensions.smallPadding))
        for (i in 1..5) {
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(i * 500)
                ),
            )

            Icon(
                imageVector = Icons.Rounded.StarRate,
                contentDescription = "Skill Icon",
                modifier = Modifier.size(UiDimensions.mediumIconSize),
                tint = if (i <= rate) MaterialTheme.colorScheme.inversePrimary.copy(alpha = alpha) else MaterialTheme.colorScheme.surfaceDim
            )
        }
    }
}


@Composable
fun SkillIcon(
    modifier: Modifier = Modifier, skillIcon: DrawableResource
) {
    Box(
        modifier = modifier.size(UiDimensions.mediumIconSize)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(skillIcon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(UiDimensions.mediumPadding)
        )
    }
}


@Composable
fun ToolsSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        SectionTitle(
            title = "ツール"
        )
        SectionContent(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolSet.tools.forEach { tool ->
                    ToolIcon(
                        modifier = Modifier.size(UiDimensions.mediumIconSize), tool = tool
                    )
                }
            }
        }
    }
}

@Composable
fun ToolIcon(
    modifier: Modifier = Modifier, tool: Tool
) {
    Box(
        modifier = modifier.size(UiDimensions.mediumIconSize)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(tool.image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(UiDimensions.mediumPadding)
        )
    }
}

@Composable
fun CareerSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        SectionTitle(
            title = "経歴"
        )
        SectionContent(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.width(15.dp), contentAlignment = Alignment.Center
            ) {
                GradationVerticalDivider(
                    modifier = Modifier.fillMaxHeight(), thickness = 5.dp, colors = listOf(
                        MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.surface
                    )
                )
            }
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                CareerByYear(modifier = Modifier.fillMaxWidth(), year = "2021年", content = {
                    BodyText(
                        text = "公立はこだて未来大学入学"
                    )
                })
                Spacer(modifier = Modifier.weight(1f))
                CareerByYear(modifier = Modifier.fillMaxWidth(), year = "2022年", content = {
                    BodyText(
                        text = "学内ハッカソンP2HacksからAndroid開発を始める"
                    )
                })
                Spacer(modifier = Modifier.weight(1f))
                CareerByYear(modifier = Modifier.fillMaxWidth(), year = "2023年", content = {
                    BodyText(
                        text = "Open Hack UでNeoHelloの開発スタート"
                    )
                    BodyText(
                        text = "NeoHelloで技育展決勝大会出場"
                    )
                })
                Spacer(modifier = Modifier.weight(1f))
                CareerByYear(modifier = Modifier.fillMaxWidth(), year = "2024年", content = {
                    BodyText(
                        text = "技育CAMP vol14でwithmoを作り優秀賞"
                    )
                    BodyText(
                        text = "技育博 5月でwithmoを発表"
                    )
                    BodyText(
                        text = "ArticleHubとChatVoxを作成"
                    )
                    BodyText(
                        text = "teamLabでインターン"
                    )
                })
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SectionTitle(
    modifier: Modifier = Modifier, title: String
) {
    Text(
        text = title,
        modifier = modifier.padding(vertical = UiDimensions.smallPadding),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun SectionSubTitle(
    modifier: Modifier = Modifier, title: String
) {
    Text(
        text = title,
        modifier = modifier.padding(vertical = UiDimensions.extraSmallPadding),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun SectionContent(
    modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.padding(horizontal = UiDimensions.mediumPadding)
    ) {
        content()
    }
}

@Composable
fun ProfileHeader(
    modifier: Modifier, profileIcon: DrawableResource, name: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(profileIcon),
            contentDescription = "Profile Icon",
            modifier = Modifier.size(UiDimensions.largeIconSize).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.padding(UiDimensions.smallPadding))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun WorksIcon(
    modifier: Modifier,
    animatedSize: Dp,
    circleColor: Color,
    interactionSource: MutableInteractionSource
) {
    Canvas(
        modifier = modifier.size(animatedSize)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        drawCircle(
            color = circleColor, radius = canvasWidth, center = Offset(canvasWidth, canvasHeight)
        )
    }

    Box(
        modifier = modifier.size(UiDimensions.largeIconSize).padding(
            horizontal = UiDimensions.largePadding, vertical = UiDimensions.largePadding
        ), contentAlignment = Alignment.BottomEnd
    ) {
        Text(
            text = "Works",
            modifier = Modifier.hoverable(interactionSource = interactionSource),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun CareerByYear(
    modifier: Modifier = Modifier, year: String, content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Circle(
                size = 15.dp, color = MaterialTheme.colorScheme.inversePrimary
            )
            Spacer(modifier = Modifier.padding(UiDimensions.extraSmallPadding))
            SectionSubTitle(
                title = year
            )
        }
        Column(
            modifier = Modifier.padding(start = 25.dp),
            verticalArrangement = Arrangement.spacedBy(UiDimensions.extraSmallPadding)
        ) {
            content()
        }
    }
}

@Composable
fun BodyText(
    modifier: Modifier = Modifier, text: String
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun Circle(
    modifier: Modifier = Modifier, size: Dp, color: Color
) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(color, CircleShape)
    )
}

@Composable
fun GradationVerticalDivider(
    modifier: Modifier = Modifier, thickness: Dp = 5.dp, colors: List<Color>
) {
    Box(
        modifier = modifier.width(thickness).background(
            brush = Brush.verticalGradient(
                colors = colors
            ), shape = CircleShape
        )
    )
}

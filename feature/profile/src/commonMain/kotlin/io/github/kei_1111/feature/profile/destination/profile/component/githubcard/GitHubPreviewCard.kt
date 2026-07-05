@file:Suppress("MagicNumber", "UnusedPrivateMember")

package io.github.kei_1111.feature.profile.destination.profile.component.githubcard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.core.designsystem.theme.KeiTheme
import io.github.kei_1111.core.designsystem.theme.keiColorScheme
import io.github.kei_1111.core.model.ContributionCalendar
import io.github.kei_1111.core.model.GitHubProfile
import io.github.kei_1111.core.model.LanguageShare
import io.github.kei_1111.core.model.LinkService
import io.github.kei_1111.core.model.PinnedRepo
import io.github.kei_1111.core.model.RepoLanguage
import io.github.kei_1111.feature.profile.IdeDimens
import io.github.kei_1111.feature.profile.destination.profile.preview.PreviewContributionCalendar
import io.github.kei_1111.feature.profile.destination.profile.preview.PreviewGitHubProfile
import kei_1111.feature.profile.generated.resources.Res
import kei_1111.feature.profile.generated.resources.img_profile_icon
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

private const val HOVER_TRANSITION_MS = 120

/**
 * GitHub プロフィール型の縦長プレビューカード（280x600）。
 * Contributions は ViewModel から渡された結果をそのまま描画する（取得中/失敗時は null）。
 */
@Composable
internal fun GitHubPreviewCard(
    profile: GitHubProfile,
    contributions: ContributionCalendar?,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(IdeDimens.GitHubCardWidth)
            .height(IdeDimens.GitHubCardHeight)
            .background(KeiTheme.colors.cardBackground)
            .border(1.dp, KeiTheme.colors.islandBorder),
    ) {
        CardHeader(
            profile = profile,
            modifier = Modifier.padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 12.dp),
        )
        StatsRow(profile = profile, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(14.dp))
        ContributionGraph(
            calendar = contributions,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        PinnedSection(
            repos = profile.pinnedRepos,
            onClickUrl = onClickUrl,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        LanguagesSection(
            languages = profile.languages,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        LinksSection(
            links = profile.links,
            onClickUrl = onClickUrl,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
        )
    }
}

/** 見出しラベル（8px・letter-spacing 0.14em・mono）。 */
@Composable
internal fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.mutedHigh).copy(letterSpacing = 1.1.sp),
    )
}

@Composable
private fun CardHeader(
    profile: GitHubProfile,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.img_profile_icon),
            contentDescription = profile.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(1.dp, KeiTheme.colors.islandBorder, CircleShape),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = profile.name,
                style = KeiTheme.typography.githubJp.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                text = "@${profile.handle} · ${profile.location}",
                style = KeiTheme.typography.chrome.copy(fontSize = 9.sp, color = KeiTheme.colors.textSecondary),
            )
            Text(
                text = profile.role,
                style = KeiTheme.typography.chrome.copy(fontSize = 9.sp, color = KeiTheme.colors.androidGreen),
            )
        }
    }
}

@Composable
private fun StatsRow(
    profile: GitHubProfile,
    modifier: Modifier = Modifier,
) {
    val numberStyle = SpanStyle(color = KeiTheme.colors.textPrimary, fontWeight = FontWeight.Bold)
    Text(
        text = buildAnnotatedString {
            withStyle(numberStyle) { append("${profile.followers}") }
            append(" followers · ")
            withStyle(numberStyle) { append("${profile.following}") }
            append(" following · ")
            withStyle(numberStyle) { append("${profile.repos}") }
            append(" repos · ★ ")
            withStyle(numberStyle) { append("${profile.totalStars}") }
        },
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(fontSize = 9.sp, color = KeiTheme.colors.textSecondary),
    )
}

@Composable
private fun PinnedSection(
    repos: ImmutableList<PinnedRepo>,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SectionLabel(text = "PINNED")
        repos.forEach { repo ->
            PinnedRepoRow(repo = repo, onClickUrl = onClickUrl)
        }
    }
}

@Composable
private fun PinnedRepoRow(
    repo: PinnedRepo,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val background by animateColorAsState(
        targetValue = if (hovered) KeiTheme.colors.gitHubItemHover else KeiTheme.colors.gitHubItem,
        animationSpec = tween(HOVER_TRANSITION_MS),
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(KeiTheme.shapes.githubItem)
            .background(background)
            .hoverable(interaction)
            .clickable { onClickUrl(repo.url) }
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = repo.name,
                style = KeiTheme.typography.chrome.copy(fontSize = 10.sp, color = KeiTheme.colors.syntaxLink),
            )
            Text(
                text = repo.description,
                style = KeiTheme.typography.githubJp.copy(fontSize = 8.sp, color = KeiTheme.colors.textSecondary),
            )
        }
        repo.language?.let { language ->
            LanguageBadge(language = language)
        }
        repo.stars?.let { stars ->
            Text(
                text = "★ $stars",
                style = KeiTheme.typography.chrome.copy(fontSize = 9.sp, color = KeiTheme.colors.textSecondary),
            )
        }
    }
}

@Composable
private fun LanguageBadge(
    language: RepoLanguage,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(language.dotColor()),
        )
        Text(
            text = language.displayName,
            style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.textSecondary),
        )
    }
}

private fun RepoLanguage.dotColor(): Color = when (this) {
    RepoLanguage.Kotlin -> keiColorScheme.langKotlin
    RepoLanguage.Swift -> keiColorScheme.langSwift
    RepoLanguage.Shell -> keiColorScheme.langShell
}

@Composable
private fun LanguagesSection(
    languages: ImmutableList<LanguageShare>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        SectionLabel(text = "LANGUAGES")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
        ) {
            languages.forEach { entry ->
                Box(
                    modifier = Modifier
                        .weight(entry.share)
                        .fillMaxHeight()
                        .background(entry.language.dotColor()),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            languages.forEach { entry ->
                LanguageShareLabel(entry = entry)
            }
        }
    }
}

@Composable
private fun LanguageShareLabel(
    entry: LanguageShare,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(entry.language.dotColor()),
        )
        Text(
            text = "${entry.language.displayName} ${(entry.share * 100).roundToInt()}%",
            style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.textSecondary),
        )
    }
}

@Composable
private fun LinksSection(
    links: ImmutableList<LinkService>,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SectionLabel(text = "LINKS")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            links.chunked(2).forEach { rowLinks ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowLinks.forEach { link ->
                        LinkTile(link = link, onClickUrl = onClickUrl, modifier = Modifier.weight(1f))
                    }
                    if (rowLinks.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkTile(
    link: LinkService,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val focused by interaction.collectIsFocusedAsState()
    val brandColor = link.type.brandColor
    val borderColor by animateColorAsState(
        targetValue = if (hovered || focused) brandColor else Color.Transparent,
        animationSpec = tween(HOVER_TRANSITION_MS),
    )
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.linkTile)
            .background(KeiTheme.colors.gitHubItem)
            .border(1.dp, borderColor, KeiTheme.shapes.linkTile)
            .hoverable(interaction)
            .clickable(interactionSource = interaction, indication = null) { onClickUrl(link.url) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(
            painter = painterResource(link.type.icon),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = brandColor,
        )
        Text(
            text = link.name,
            style = KeiTheme.typography.githubJp.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
        )
    }
}

@Preview
@Composable
private fun GitHubPreviewCardPreview() {
    KeiTheme {
        GitHubPreviewCard(
            profile = PreviewGitHubProfile,
            contributions = PreviewContributionCalendar,
            onClickUrl = {},
        )
    }
}

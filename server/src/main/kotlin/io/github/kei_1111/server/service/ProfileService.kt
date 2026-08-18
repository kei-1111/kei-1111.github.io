package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.LanguageBytes
import io.github.kei_1111.server.client.PROFILE_LOGIN
import io.github.kei_1111.server.client.PinnedRepoSource
import io.github.kei_1111.server.client.ProfileStats
import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.client.PublishedProfile
import io.github.kei_1111.server.client.PublishedResult
import io.github.kei_1111.server.client.descriptionOverrides
import io.github.kei_1111.server.client.fetchProfileStats
import io.github.kei_1111.server.client.hidesPinnedRepo
import io.github.kei_1111.server.client.links
import io.github.kei_1111.server.client.localized
import io.github.kei_1111.server.client.valueOrNull
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.Profile
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.round

private const val MIN_LANGUAGE_SHARE = 0.01f
private const val MAX_LANGUAGE_COUNT = 5
private const val SHARE_ROUNDING_SCALE = 100f

internal fun languageSharesFrom(languageSizes: List<LanguageBytes>): ImmutableList<LanguageShare> {
    val totalSize = languageSizes.sumOf { it.size }
    if (totalSize == 0L) return persistentListOf()

    return languageSizes
        .filter { it.size.toFloat() / totalSize.toFloat() >= MIN_LANGUAGE_SHARE }
        .sortedByDescending { it.size }
        .take(MAX_LANGUAGE_COUNT)
        .map { language ->
            val ratio = language.size.toFloat() / totalSize.toFloat()
            LanguageShare(
                language = RepoLanguage(language.name),
                share = round(ratio * SHARE_ROUNDING_SCALE) / SHARE_ROUNDING_SCALE,
                color = language.color,
            )
        }
        .toImmutableList()
}

internal fun pinnedReposFrom(
    pinnedRepos: List<PinnedRepoSource>,
    descriptions: Map<String, LocalizedText>,
): ImmutableList<PinnedRepo> = pinnedRepos.map { repo ->
    PinnedRepo(
        name = repo.name,
        description = descriptions[repo.name]
            ?: LocalizedText(ja = repo.description.orEmpty(), en = repo.description.orEmpty()),
        url = repo.url,
        language = repo.languageName?.let(::RepoLanguage),
        stars = repo.stars.takeIf { it > 0 },
    )
}.toImmutableList()

internal fun PublishedProfile.toProfile(stats: ProfileStats?): Profile = Profile(
    name = localized(ja = displayName, en = displayNameEn),
    handle = PROFILE_LOGIN,
    location = location,
    role = role,
    iconUrl = avatarUrl.ifBlank { null },
    followers = stats?.followers,
    following = stats?.following,
    repos = stats?.repos,
    totalStars = stats?.totalStars,
    pinnedRepos = pinnedReposFrom(
        pinnedRepos = stats?.pinnedRepos.orEmpty().filterNot { hidesPinnedRepo(it.name) },
        descriptions = descriptionOverrides(),
    ),
    languages = languageSharesFrom(stats?.languageSizes.orEmpty()),
    links = links().toImmutableList(),
)

internal class ProfileService(
    private val gitHubClient: GitHubClient,
    private val publishedContentClient: PublishedContentClient,
) {
    private val statsCache = TtlCache<ProfileStats>(GITHUB_DATA_TTL_MILLIS, name = "profile-stats")
    private val publishedCache =
        TtlCache<PublishedResult<PublishedProfile>>(PUBLISHED_CONTENT_TTL_MILLIS, name = "published-profile")

    suspend fun getProfile(): Profile? = coroutineScope {
        val statsDeferred = async { statsCache.get { gitHubClient.fetchProfileStats() } }
        val publishedDeferred = async { publishedCache.get { publishedContentClient.fetchProfile() } }
        val stats = statsDeferred.await()
        publishedDeferred.await().valueOrNull()?.toProfile(stats)
    }
}

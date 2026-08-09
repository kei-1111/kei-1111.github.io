package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.LanguageBytes
import io.github.kei_1111.server.client.PinnedRepoSource
import io.github.kei_1111.server.client.ProfileStats
import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.client.PublishedProfile
import io.github.kei_1111.server.client.PublishedResult
import io.github.kei_1111.server.client.fetchProfileStats
import io.github.kei_1111.server.client.overlayOn
import io.github.kei_1111.server.client.valueOrNull
import io.github.kei_1111.server.content.DefaultGitHubProfile
import io.github.kei_1111.server.content.PinnedRepoDescriptions
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.PinnedRepo
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

internal class ProfileService(
    private val gitHubClient: GitHubClient,
    private val publishedContentClient: PublishedContentClient,
) {
    private val statsCache = TtlCache<ProfileStats>(GITHUB_DATA_TTL_MILLIS, name = "profile-stats")
    private val publishedCache =
        TtlCache<PublishedResult<PublishedProfile>>(PUBLISHED_CONTENT_TTL_MILLIS, name = "published-profile")

    suspend fun getProfile(): GitHubProfile = coroutineScope {
        val statsDeferred = async { statsCache.get { gitHubClient.fetchProfileStats() } }
        val publishedDeferred = async { publishedCache.get { publishedContentClient.fetchProfile() } }
        val stats = statsDeferred.await()
        val base = if (stats != null) {
            val languages = languageSharesFrom(stats.languageSizes).ifEmpty { DefaultGitHubProfile.languages }
            val pinnedRepos = pinnedReposFrom(stats.pinnedRepos, PinnedRepoDescriptions)
                .ifEmpty { DefaultGitHubProfile.pinnedRepos }
            DefaultGitHubProfile.copy(
                followers = stats.followers,
                following = stats.following,
                repos = stats.repos,
                totalStars = stats.totalStars,
                pinnedRepos = pinnedRepos,
                languages = languages,
            )
        } else {
            DefaultGitHubProfile
        }
        val published = publishedDeferred.await().valueOrNull()
        published?.overlayOn(base) ?: base
    }
}

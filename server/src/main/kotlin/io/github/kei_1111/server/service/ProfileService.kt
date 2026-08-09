package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.LanguageBytes
import io.github.kei_1111.server.client.ProfileStats
import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.client.PublishedProfile
import io.github.kei_1111.server.client.fetchProfileStats
import io.github.kei_1111.server.client.overlayOn
import io.github.kei_1111.server.content.DefaultGitHubProfile
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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

class ProfileService(
    private val gitHubClient: GitHubClient,
    private val publishedContentClient: PublishedContentClient,
) {
    private val statsCache = TtlCache<ProfileStats>(STATS_TTL_MILLIS, name = "profile-stats")
    private val publishedCache = TtlCache<PublishedProfile>(PUBLISHED_TTL_MILLIS, name = "published-profile")

    suspend fun getProfile(): GitHubProfile {
        val stats = statsCache.get { gitHubClient.fetchProfileStats() }
        val base = if (stats != null) {
            val languages = languageSharesFrom(stats.languageSizes).ifEmpty { DefaultGitHubProfile.languages }
            DefaultGitHubProfile.copy(
                followers = stats.followers,
                following = stats.following,
                repos = stats.repos,
                totalStars = stats.totalStars,
                languages = languages,
            )
        } else {
            DefaultGitHubProfile
        }
        val published = publishedCache.get { publishedContentClient.fetchProfile() }
        return published?.overlayOn(base) ?: base
    }

    companion object {
        // GitHub API のレートリミット消費を抑えつつ、統計のずれが目立たない程度の鮮度に保つ TTL。
        private const val STATS_TTL_MILLIS = 10L * 60L * 1000L

        // コンテンツ更新は低頻度のため、GCS 読み出しを抑えつつ公開後数分で反映される鮮度に保つ TTL。
        private const val PUBLISHED_TTL_MILLIS = 5L * 60L * 1000L
    }
}

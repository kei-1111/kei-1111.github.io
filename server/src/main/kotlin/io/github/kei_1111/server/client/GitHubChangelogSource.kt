package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubPullRequest
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

// GraphQL の PullRequestOrderField に MERGED_AT は無いため CREATED_AT 降順で取得して
// mergedAt へ並べ直す。branch-per-issue の短命ブランチ運用では作成順とマージ順がほぼ一致し、
// 先頭 100 件からの取りこぼしは実質発生しない。
internal val MERGED_PULL_REQUESTS_QUERY = """
    query(${'$'}owner: String!, ${'$'}name: String!) {
      repository(owner: ${'$'}owner, name: ${'$'}name) {
        pullRequests(
          states: MERGED,
          baseRefName: "main",
          first: 100,
          orderBy: {field: CREATED_AT, direction: DESC}
        ) {
          totalCount
          nodes { number title url headRefName mergedAt }
        }
      }
    }
""".trimIndent()

@Serializable
internal data class ChangelogData(val repository: ChangelogRepositoryNode? = null)

@Serializable
internal data class ChangelogRepositoryNode(val pullRequests: PullRequestConnectionNode)

@Serializable
internal data class PullRequestConnectionNode(
    val totalCount: Int = 0,
    val nodes: List<PullRequestNode> = emptyList(),
)

@Serializable
internal data class PullRequestNode(
    val number: Int,
    val title: String,
    val url: String,
    val headRefName: String,
    val mergedAt: String? = null,
)

// このリポジトリの Pull Request タイトル規約 `[<Type>]: <title>`(.claude/rules/git-workflow.md)を種別と表題に分解する。
private val TYPE_PREFIX_REGEX = Regex("""^\[(.+?)]:\s*(.*)$""")

private val logger = LoggerFactory.getLogger("io.github.kei_1111.server.client.GitHubChangelogSource")

internal suspend fun GitHubClient.fetchMergedPullRequests(): GitHubChangelog? {
    val variables = mapOf("owner" to PROFILE_LOGIN, "name" to REPO_NAME)
    return execute<ChangelogData>(MERGED_PULL_REQUESTS_QUERY, variables)
        ?.repositoryOrWarn()
        ?.pullRequests
        ?.toGitHubChangelog()
}

// HTTP 200 かつ errors なしでも repository は null になり得る(リポジトリ改名やトークンのスコープ不足)。
private fun ChangelogData.repositoryOrWarn(): ChangelogRepositoryNode? {
    if (repository == null) {
        logger.warn("GitHub GraphQL API returned a null repository for '{}/{}'", PROFILE_LOGIN, REPO_NAME)
    }
    return repository
}

private fun PullRequestConnectionNode.toGitHubChangelog(): GitHubChangelog = GitHubChangelog(
    totalCount = totalCount,
    pullRequests = nodes
        .mapNotNull { it.toGitHubPullRequest() }
        .sortedByDescending { it.mergedAt }
        .toImmutableList(),
)

private fun PullRequestNode.toGitHubPullRequest(): GitHubPullRequest? {
    val mergedAt = mergedAt ?: return null
    val match = TYPE_PREFIX_REGEX.matchEntire(title)
    return GitHubPullRequest(
        number = number,
        title = match?.groupValues?.get(2) ?: title,
        url = url,
        headRefName = headRefName,
        mergedAt = mergedAt,
        type = match?.groupValues?.get(1),
    )
}

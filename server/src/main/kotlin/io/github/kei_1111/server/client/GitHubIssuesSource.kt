package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.GitHubIssue
import io.github.kei_1111.shared.model.GitHubIssues
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

// PROFILE_LOGIN と同じ理由で対象リポジトリも固定する(公開 endpoint で任意リポジトリを受けない)。
internal const val REPO_NAME = "kei-1111.github.io"

// TODO ツールウィンドウは「これから着手する項目」を見せる場なので open Issue のみを新しい順で返す。
internal val OPEN_ISSUES_QUERY = """
    query(${'$'}owner: String!, ${'$'}name: String!) {
      repository(owner: ${'$'}owner, name: ${'$'}name) {
        issues(states: OPEN, first: 50, orderBy: {field: CREATED_AT, direction: DESC}) {
          totalCount
          nodes { number title url }
        }
      }
    }
""".trimIndent()

@Serializable
internal data class IssuesData(val repository: IssuesRepositoryNode? = null)

@Serializable
internal data class IssuesRepositoryNode(val issues: IssueConnectionNode)

@Serializable
internal data class IssueConnectionNode(
    val totalCount: Int = 0,
    val nodes: List<IssueNode> = emptyList(),
)

@Serializable
internal data class IssueNode(
    val number: Int,
    val title: String,
    val url: String,
)

// このリポジトリの Issue タイトル規約 `[<Type>]: <title>`(.claude/rules/git-workflow.md)を種別と表題に分解する。
private val TYPE_PREFIX_REGEX = Regex("""^\[(.+?)]:\s*(.*)$""")

private val logger = LoggerFactory.getLogger("io.github.kei_1111.server.client.GitHubIssuesSource")

internal suspend fun GitHubClient.fetchOpenIssues(): GitHubIssues? {
    val variables = mapOf("owner" to PROFILE_LOGIN, "name" to REPO_NAME)
    return execute<IssuesData>(OPEN_ISSUES_QUERY, variables)
        ?.repositoryOrWarn()
        ?.issues
        ?.toGitHubIssues()
}

// HTTP 200 かつ errors なしでも repository は null になり得る(リポジトリ改名やトークンのスコープ不足)。
private fun IssuesData.repositoryOrWarn(): IssuesRepositoryNode? {
    if (repository == null) {
        logger.warn("GitHub GraphQL API returned a null repository for '{}/{}'", PROFILE_LOGIN, REPO_NAME)
    }
    return repository
}

private fun IssueConnectionNode.toGitHubIssues(): GitHubIssues = GitHubIssues(
    totalCount = totalCount,
    issues = nodes.map { it.toGitHubIssue() }.toImmutableList(),
)

private fun IssueNode.toGitHubIssue(): GitHubIssue {
    val match = TYPE_PREFIX_REGEX.matchEntire(title)
    return GitHubIssue(
        number = number,
        title = match?.groupValues?.get(2) ?: title,
        url = url,
        type = match?.groupValues?.get(1),
    )
}

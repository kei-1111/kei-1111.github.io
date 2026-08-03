package io.github.kei_1111.shared.model.serialization

import io.github.kei_1111.shared.model.GitHubIssue
import io.github.kei_1111.shared.model.GitHubIssues
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

private val json = Json

class ImmutableListSerializerTest {

    @Test
    fun roundTripPreservesRepresentativePayload() {
        val issues = GitHubIssues(
            totalCount = 2,
            issues = persistentListOf(
                GitHubIssue(
                    number = 106,
                    title = "Add a TODO tool window showing the repository's real open Issues",
                    url = "https://github.com/kei-1111/kei-1111.github.io/issues/106",
                    type = "Feature",
                ),
                GitHubIssue(
                    number = 24,
                    title = "作品ページの追加（作品 API + クライアント UI）",
                    url = "https://github.com/kei-1111/kei-1111.github.io/issues/24",
                ),
            ),
        )

        val decoded = json.decodeFromString<GitHubIssues>(json.encodeToString(GitHubIssues.serializer(), issues))

        assertEquals(issues, decoded)
    }
}

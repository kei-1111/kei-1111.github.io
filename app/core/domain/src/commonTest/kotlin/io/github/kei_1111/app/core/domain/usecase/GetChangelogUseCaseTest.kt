package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.ChangelogRepository
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubPullRequest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetChangelogUseCaseTest {

    @Test
    fun forwardsTheRepositoryFlow() = runTest {
        val expected = changelog(number = 205)
        val useCase = GetChangelogUseCaseImpl(FakeChangelogRepository(flowOf(expected)))

        val actual = useCase().toList()

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun collapsesConsecutiveDuplicateEmissions() = runTest {
        val first = changelog(number = 204)
        val duplicate = changelog(number = 204)
        val second = changelog(number = 205)
        val useCase = GetChangelogUseCaseImpl(
            FakeChangelogRepository(flowOf(first, duplicate, second, first)),
        )

        val actual = useCase().toList()

        assertEquals(listOf(first, second, first), actual)
    }
}

private class FakeChangelogRepository(override val changelog: Flow<GitHubChangelog>) : ChangelogRepository

private fun changelog(number: Int) = GitHubChangelog(
    totalCount = 1,
    pullRequests = persistentListOf(
        GitHubPullRequest(
            number = number,
            title = "Pull request $number",
            url = "https://github.com/kei-1111/kei-1111.github.io/pull/$number",
            headRefName = "feature/$number",
            mergedAt = "2026-08-09T02:00:00Z",
            type = "Feature",
        ),
    ),
)

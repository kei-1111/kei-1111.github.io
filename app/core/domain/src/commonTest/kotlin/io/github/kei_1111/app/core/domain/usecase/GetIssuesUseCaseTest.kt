package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.IssuesRepository
import io.github.kei_1111.shared.model.GitHubIssues
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetIssuesUseCaseTest {

    @Test
    fun forwardsTheRepositoryFlow() = runTest {
        val expected = issues(totalCount = 5)
        val useCase = GetIssuesUseCaseImpl(FakeIssuesRepository(flowOf(expected)))

        val actual = useCase().toList()

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun collapsesConsecutiveDuplicateEmissions() = runTest {
        val first = issues(totalCount = 5)
        val duplicate = issues(totalCount = 5)
        val second = issues(totalCount = 6)
        val useCase = GetIssuesUseCaseImpl(FakeIssuesRepository(flowOf(first, duplicate, second, first)))

        val actual = useCase().toList()

        assertEquals(listOf(first, second, first), actual)
    }
}

private class FakeIssuesRepository(
    override val issues: Flow<GitHubIssues>,
) : IssuesRepository

private fun issues(totalCount: Int) = GitHubIssues(
    totalCount = totalCount,
    issues = persistentListOf(),
)

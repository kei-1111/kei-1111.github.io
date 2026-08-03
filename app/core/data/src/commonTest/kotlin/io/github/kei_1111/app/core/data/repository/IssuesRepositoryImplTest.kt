package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.api.issues.IssuesApi
import io.github.kei_1111.shared.model.GitHubIssues
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class IssuesRepositoryImplTest {

    @Test
    fun emitsTheFetchedValue() = runTest {
        val expected = issues()
        val repository = IssuesRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            issuesApi = FakeIssuesApi(expected),
        )

        val actual = repository.issues.first()

        assertEquals(expected, actual)
    }

    @Test
    fun throwsWhenTheFetchFails() = runTest {
        val repository = IssuesRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            issuesApi = FakeIssuesApi(null),
        )

        assertFailsWith<IllegalStateException> {
            repository.issues.first()
        }
    }

    @Test
    fun sharesOneFetchAcrossCollections() = runTest {
        val api = FakeIssuesApi(issues())
        val repository = IssuesRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            issuesApi = api,
        )

        repository.issues.first()
        repository.issues.first()

        assertEquals(1, api.callCount)
    }
}

private class FakeIssuesApi(
    private val result: GitHubIssues?,
) : IssuesApi {
    var callCount = 0

    override suspend fun fetchIssues(): GitHubIssues? {
        callCount += 1
        return result
    }
}

private fun issues() = GitHubIssues(
    totalCount = 0,
    issues = persistentListOf(),
)

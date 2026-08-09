package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.api.changelog.ChangelogApi
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubPullRequest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class ChangelogRepositoryImplTest {

    @Test
    fun emitsTheFetchedValue() = runTest {
        val expected = changelog()
        val repository = ChangelogRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            changelogApi = FakeChangelogApi(expected),
        )

        val actual = repository.changelog.first()

        assertEquals(expected, actual)
    }

    @Test
    fun throwsWhenTheFetchFails() = runTest {
        val repository = ChangelogRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            changelogApi = FakeChangelogApi(null),
        )

        assertFailsWith<IllegalStateException> {
            repository.changelog.first()
        }
    }

    @Test
    fun throwsWhenTheFetchSucceedsWithAnEmptyChangelog() = runTest {
        val repository = ChangelogRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            changelogApi = FakeChangelogApi(
                GitHubChangelog(totalCount = 0, pullRequests = persistentListOf()),
            ),
        )

        assertFailsWith<IllegalStateException> {
            repository.changelog.first()
        }
    }

    @Test
    fun sharesOneFetchAcrossCollections() = runTest {
        val api = FakeChangelogApi(changelog())
        val repository = ChangelogRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            changelogApi = api,
        )

        repository.changelog.first()
        repository.changelog.first()

        assertEquals(1, api.callCount)
    }

    @Test
    fun refetchesAfterAnEmptyResponseInsteadOfCachingIt() = runTest {
        val api = FakeChangelogApi(GitHubChangelog(totalCount = 0, pullRequests = persistentListOf()))
        val repository = ChangelogRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            changelogApi = api,
        )

        assertFailsWith<IllegalStateException> {
            repository.changelog.first()
        }

        api.result = changelog()

        assertEquals(changelog(), repository.changelog.first())
        assertEquals(2, api.callCount)
    }
}

private class FakeChangelogApi(
    var result: GitHubChangelog?,
) : ChangelogApi {
    var callCount = 0

    override suspend fun fetchChangelog(): GitHubChangelog? {
        callCount += 1
        return result
    }
}

private fun changelog() = GitHubChangelog(
    totalCount = 1,
    pullRequests = persistentListOf(
        GitHubPullRequest(
            number = 205,
            title = "Add changelog data chain",
            url = "https://github.com/kei-1111/kei-1111.github.io/pull/205",
            headRefName = "feature/205",
            mergedAt = "2026-08-09T02:00:00Z",
            type = "Feature",
        ),
    ),
)

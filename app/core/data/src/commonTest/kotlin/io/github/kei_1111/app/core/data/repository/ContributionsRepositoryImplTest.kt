package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.api.contributions.ContributionsApi
import io.github.kei_1111.shared.model.ContributionCalendar
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class ContributionsRepositoryImplTest {

    @Test
    fun emitsTheFetchedValue() = runTest {
        val expected = contributionCalendar()
        val repository = ContributionsRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            contributionsApi = FakeContributionsApi(expected),
        )

        val actual = repository.contributions.first()

        assertEquals(expected, actual)
    }

    @Test
    fun throwsWhenTheFetchFails() = runTest {
        val repository = ContributionsRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            contributionsApi = FakeContributionsApi(null),
        )

        assertFailsWith<IllegalStateException> {
            repository.contributions.first()
        }
    }

    @Test
    fun sharesOneFetchAcrossCollections() = runTest {
        val api = FakeContributionsApi(contributionCalendar())
        val repository = ContributionsRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            contributionsApi = api,
        )

        repository.contributions.first()
        repository.contributions.first()

        assertEquals(1, api.callCount)
    }
}

private class FakeContributionsApi(
    private val result: ContributionCalendar?,
) : ContributionsApi {
    var callCount = 0

    override suspend fun fetchContributions(): ContributionCalendar? {
        callCount += 1
        return result
    }
}

private fun contributionCalendar() = ContributionCalendar(
    totalLastYear = 0,
    days = persistentListOf(),
)

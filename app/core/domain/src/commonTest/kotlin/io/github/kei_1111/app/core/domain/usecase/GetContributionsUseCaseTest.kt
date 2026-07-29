package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.ContributionsRepository
import io.github.kei_1111.shared.model.ContributionCalendar
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetContributionsUseCaseTest {

    @Test
    fun forwardsTheRepositoryFlow() = runTest {
        val expected = calendar(totalLastYear = 100)
        val useCase = GetContributionsUseCaseImpl(FakeContributionsRepository(flowOf(expected)))

        val actual = useCase().toList()

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun collapsesConsecutiveDuplicateEmissions() = runTest {
        val first = calendar(totalLastYear = 100)
        val duplicate = calendar(totalLastYear = 100)
        val second = calendar(totalLastYear = 200)
        val useCase = GetContributionsUseCaseImpl(FakeContributionsRepository(flowOf(first, duplicate, second, first)))

        val actual = useCase().toList()

        assertEquals(listOf(first, second, first), actual)
    }
}

private class FakeContributionsRepository(
    override val contributions: Flow<ContributionCalendar>,
) : ContributionsRepository

private fun calendar(totalLastYear: Int) = ContributionCalendar(
    totalLastYear = totalLastYear,
    days = persistentListOf(),
)

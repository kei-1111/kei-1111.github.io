package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetLastNotifiedPrNumberUseCaseTest {

    @Test
    fun forwardsTheRepositoryFlow() = runTest {
        val useCase = GetLastNotifiedPrNumberUseCaseImpl(
            FakeGetNotificationRepository(flowOf(206)),
        )

        val actual = useCase().toList()

        assertEquals(listOf(206), actual)
    }

    @Test
    fun collapsesConsecutiveDuplicateEmissions() = runTest {
        val useCase = GetLastNotifiedPrNumberUseCaseImpl(
            FakeGetNotificationRepository(flowOf(206, 206, 207, 206)),
        )

        val actual = useCase().toList()

        assertEquals(listOf(206, 207, 206), actual)
    }
}

private class FakeGetNotificationRepository(
    override val lastNotifiedPrNumber: Flow<Int?>,
) : NotificationRepository {
    override suspend fun saveLastNotifiedPrNumber(prNumber: Int) = Unit
}

package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLastNotifiedPrNumberUseCaseTest {

    @Test
    fun delegatesTheGivenPrNumberToTheRepository() = runTest {
        val repository = FakeSaveNotificationRepository()
        val useCase = SaveLastNotifiedPrNumberUseCaseImpl(repository)

        useCase(206)

        assertEquals(listOf(206), repository.savedPrNumbers)
    }
}

private class FakeSaveNotificationRepository : NotificationRepository {
    override val lastNotifiedPrNumber: Flow<Int?> = flowOf(null)
    val savedPrNumbers = mutableListOf<Int>()

    override suspend fun saveLastNotifiedPrNumber(prNumber: Int) {
        savedPrNumbers += prNumber
    }
}

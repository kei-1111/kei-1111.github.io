package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.local.notification.NotificationLocalDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationRepositoryImplTest {

    @Test
    fun passesThroughTheLastNotifiedPrNumberFromTheDataSource() = runTest {
        val repository = NotificationRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            notificationLocalDataSource = FakeNotificationLocalDataSource(flowOf(206)),
        )

        val prNumber = repository.lastNotifiedPrNumber.first()

        assertEquals(206, prNumber)
    }

    @Test
    fun passesThroughNullFromTheDataSource() = runTest {
        val repository = NotificationRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            notificationLocalDataSource = FakeNotificationLocalDataSource(flowOf(null)),
        )

        val prNumber = repository.lastNotifiedPrNumber.first()

        assertNull(prNumber)
    }

    @Test
    fun delegatesSavingToTheDataSource() = runTest {
        val dataSource = FakeNotificationLocalDataSource(flowOf(null))
        val repository = NotificationRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            notificationLocalDataSource = dataSource,
        )

        repository.saveLastNotifiedPrNumber(206)

        assertEquals(listOf(206), dataSource.savedPrNumbers)
    }
}

private class FakeNotificationLocalDataSource(
    override val lastNotifiedPrNumber: Flow<Int?>,
) : NotificationLocalDataSource {
    val savedPrNumbers = mutableListOf<Int>()

    override suspend fun saveLastNotifiedPrNumber(prNumber: Int) {
        savedPrNumbers += prNumber
    }
}

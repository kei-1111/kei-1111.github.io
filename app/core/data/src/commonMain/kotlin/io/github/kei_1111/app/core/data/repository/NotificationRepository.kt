package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.local.notification.NotificationLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

interface NotificationRepository {
    val lastNotifiedPrNumber: Flow<Int?>

    suspend fun saveLastNotifiedPrNumber(prNumber: Int)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class NotificationRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val notificationLocalDataSource: NotificationLocalDataSource,
) : NotificationRepository {

    override val lastNotifiedPrNumber: Flow<Int?> =
        notificationLocalDataSource.lastNotifiedPrNumber.flowOn(defaultDispatcher)

    override suspend fun saveLastNotifiedPrNumber(prNumber: Int) {
        notificationLocalDataSource.saveLastNotifiedPrNumber(prNumber)
    }
}

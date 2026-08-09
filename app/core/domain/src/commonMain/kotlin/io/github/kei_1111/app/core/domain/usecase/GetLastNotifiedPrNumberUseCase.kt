package io.github.kei_1111.app.core.domain.usecase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.data.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface GetLastNotifiedPrNumberUseCase {
    operator fun invoke(): Flow<Int?>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetLastNotifiedPrNumberUseCaseImpl(
    private val notificationRepository: NotificationRepository,
) : GetLastNotifiedPrNumberUseCase {
    override fun invoke(): Flow<Int?> =
        notificationRepository.lastNotifiedPrNumber
            .distinctUntilChanged()
}

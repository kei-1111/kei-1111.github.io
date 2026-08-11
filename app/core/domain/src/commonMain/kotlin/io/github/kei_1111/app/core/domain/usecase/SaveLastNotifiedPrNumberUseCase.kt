package io.github.kei_1111.app.core.domain.usecase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.data.repository.NotificationRepository

interface SaveLastNotifiedPrNumberUseCase {
    suspend operator fun invoke(prNumber: Int)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class SaveLastNotifiedPrNumberUseCaseImpl(
    private val notificationRepository: NotificationRepository,
) : SaveLastNotifiedPrNumberUseCase {
    override suspend fun invoke(prNumber: Int) {
        notificationRepository.saveLastNotifiedPrNumber(prNumber)
    }
}

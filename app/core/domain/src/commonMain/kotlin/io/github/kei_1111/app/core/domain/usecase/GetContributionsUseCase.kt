package io.github.kei_1111.app.core.domain.usecase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.data.repository.ContributionsRepository
import io.github.kei_1111.shared.model.ContributionCalendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface GetContributionsUseCase {
    operator fun invoke(): Flow<ContributionCalendar>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetContributionsUseCaseImpl(
    private val contributionsRepository: ContributionsRepository,
) : GetContributionsUseCase {
    override fun invoke(): Flow<ContributionCalendar> =
        contributionsRepository.getContributions()
            .distinctUntilChanged()
}

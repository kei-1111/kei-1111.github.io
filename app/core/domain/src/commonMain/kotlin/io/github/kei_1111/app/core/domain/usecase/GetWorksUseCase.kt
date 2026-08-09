package io.github.kei_1111.app.core.domain.usecase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.data.repository.WorksRepository
import io.github.kei_1111.shared.model.Works
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface GetWorksUseCase {
    operator fun invoke(): Flow<Works>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetWorksUseCaseImpl(
    private val worksRepository: WorksRepository,
) : GetWorksUseCase {
    override fun invoke(): Flow<Works> =
        worksRepository.works
            .distinctUntilChanged()
}

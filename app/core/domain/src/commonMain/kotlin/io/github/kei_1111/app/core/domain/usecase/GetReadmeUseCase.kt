package io.github.kei_1111.app.core.domain.usecase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.data.repository.ReadmeRepository
import io.github.kei_1111.shared.model.Readme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface GetReadmeUseCase {
    operator fun invoke(): Flow<Readme>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetReadmeUseCaseImpl(
    private val readmeRepository: ReadmeRepository,
) : GetReadmeUseCase {
    override fun invoke(): Flow<Readme> =
        readmeRepository.readme
            .distinctUntilChanged()
}

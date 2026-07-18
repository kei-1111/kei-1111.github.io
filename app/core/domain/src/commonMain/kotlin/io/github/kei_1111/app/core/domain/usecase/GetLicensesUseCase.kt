package io.github.kei_1111.app.core.domain.usecase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.data.repository.LicensesRepository
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface GetLicensesUseCase {
    operator fun invoke(): Flow<ThirdPartyLicenses>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetLicensesUseCaseImpl(
    private val licensesRepository: LicensesRepository,
) : GetLicensesUseCase {
    override fun invoke(): Flow<ThirdPartyLicenses> =
        licensesRepository.licenses
            .distinctUntilChanged()
}

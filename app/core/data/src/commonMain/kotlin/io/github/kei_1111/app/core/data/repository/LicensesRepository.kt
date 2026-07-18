package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.data.license.LicenseContent
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface LicensesRepository {
    val licenses: Flow<ThirdPartyLicenses>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class LicensesRepositoryImpl : LicensesRepository {
    override val licenses: Flow<ThirdPartyLicenses> = flowOf(LicenseContent.licenses)
}

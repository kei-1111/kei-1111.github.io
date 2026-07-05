package io.github.kei_1111.core.domain.usecase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.core.data.repository.ProfileRepository
import io.github.kei_1111.core.model.GitHubProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface GetProfileUseCase {
    operator fun invoke(): Flow<GitHubProfile>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetProfileUseCaseImpl(
    private val profileRepository: ProfileRepository,
) : GetProfileUseCase {
    override fun invoke(): Flow<GitHubProfile> =
        profileRepository.profile
            .distinctUntilChanged()
}

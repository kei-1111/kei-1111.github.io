package io.github.kei_1111.app.feature.profile.fake

import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.shared.model.GitHubProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/** [GetProfileUseCase] の手書き fake。テストが [emit] するまで何も流さない。 */
internal class FakeGetProfileUseCase : GetProfileUseCase {
    private val profileFlow = MutableSharedFlow<GitHubProfile>(replay = 1)

    override fun invoke(): Flow<GitHubProfile> = profileFlow

    suspend fun emit(profile: GitHubProfile) {
        profileFlow.emit(profile)
    }
}

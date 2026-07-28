package io.github.kei_1111.app.feature.profile.fake

import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.shared.model.GitHubProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

/** [GetProfileUseCase] の手書き fake。テストが [emit] か [emitFailure] するまで何も流さない。 */
internal class FakeGetProfileUseCase : GetProfileUseCase {
    private val results = MutableSharedFlow<Result<GitHubProfile>>(replay = 1)

    override fun invoke(): Flow<GitHubProfile> = results.map { it.getOrThrow() }

    suspend fun emit(profile: GitHubProfile) = results.emit(Result.success(profile))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

package io.github.kei_1111.app.feature.profile.fake

import io.github.kei_1111.app.core.domain.usecase.GetProfileUseCase
import io.github.kei_1111.shared.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

internal class FakeGetProfileUseCase : GetProfileUseCase {
    private val results = MutableSharedFlow<Result<Profile>>(replay = 1)

    override fun invoke(): Flow<Profile> = results.map { it.getOrThrow() }

    suspend fun emit(profile: Profile) = results.emit(Result.success(profile))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

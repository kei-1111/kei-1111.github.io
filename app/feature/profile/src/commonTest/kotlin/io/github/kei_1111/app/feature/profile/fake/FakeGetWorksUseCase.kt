package io.github.kei_1111.app.feature.profile.fake

import io.github.kei_1111.app.core.domain.usecase.GetWorksUseCase
import io.github.kei_1111.shared.model.Work
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

internal class FakeGetWorksUseCase : GetWorksUseCase {
    private val results = MutableSharedFlow<Result<List<Work>>>(replay = 1)

    override fun invoke(): Flow<List<Work>> = results.map { it.getOrThrow() }

    suspend fun emit(works: List<Work>) = results.emit(Result.success(works))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

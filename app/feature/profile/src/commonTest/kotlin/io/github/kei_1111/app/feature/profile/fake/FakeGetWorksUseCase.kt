package io.github.kei_1111.app.feature.profile.fake

import io.github.kei_1111.app.core.domain.usecase.GetWorksUseCase
import io.github.kei_1111.shared.model.Works
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

internal class FakeGetWorksUseCase : GetWorksUseCase {
    private val results = MutableSharedFlow<Result<Works>>(replay = 1)

    override fun invoke(): Flow<Works> = results.map { it.getOrThrow() }

    suspend fun emit(works: Works) = results.emit(Result.success(works))

    suspend fun emitFailure(exception: Throwable) = results.emit(Result.failure(exception))
}

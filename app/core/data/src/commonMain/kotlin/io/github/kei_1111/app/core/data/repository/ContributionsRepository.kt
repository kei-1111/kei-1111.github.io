package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.cache.SingleFlightCache
import io.github.kei_1111.app.core.data.contributions.FallbackContributions
import io.github.kei_1111.app.core.data.contributions.parseContributions
import io.github.kei_1111.app.core.data.network.API_BASE_URL
import io.github.kei_1111.app.core.data.network.fetchText
import io.github.kei_1111.shared.model.ContributionCalendar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface ContributionsRepository {
    fun getContributions(): Flow<ContributionCalendar>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ContributionsRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ContributionsRepository {

    private val cache = SingleFlightCache(defaultDispatcher) {
        fetchText("$API_BASE_URL/api/contributions")?.let(::parseContributions)
    }

    override fun getContributions(): Flow<ContributionCalendar> = flow {
        emit(cache.get() ?: FallbackContributions.calendar)
    }.flowOn(defaultDispatcher)
}

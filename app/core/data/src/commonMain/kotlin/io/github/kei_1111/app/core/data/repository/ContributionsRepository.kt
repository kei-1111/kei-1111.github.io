package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.contributions.CONTRIBUTIONS_API
import io.github.kei_1111.app.core.data.contributions.FallbackContributions
import io.github.kei_1111.app.core.data.contributions.fetchText
import io.github.kei_1111.app.core.data.contributions.parseContributions
import io.github.kei_1111.shared.model.ContributionCalendar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface ContributionsRepository {
    fun getContributions(user: String): Flow<ContributionCalendar>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ContributionsRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ContributionsRepository {

    override fun getContributions(user: String): Flow<ContributionCalendar> = flow {
        val live = fetchText("$CONTRIBUTIONS_API$user?y=last")?.let(::parseContributions)
        emit(live ?: FallbackContributions.calendar)
    }.flowOn(defaultDispatcher)
}

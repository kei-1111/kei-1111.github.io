package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.cache.SingleFlightCache
import io.github.kei_1111.app.core.data.network.API_BASE_URL
import io.github.kei_1111.app.core.data.network.fetchText
import io.github.kei_1111.app.core.data.profile.FallbackProfile
import io.github.kei_1111.app.core.data.profile.parseProfile
import io.github.kei_1111.shared.model.GitHubProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface ProfileRepository {
    val profile: Flow<GitHubProfile>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ProfileRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ProfileRepository {

    private val cache = SingleFlightCache(defaultDispatcher) {
        fetchText("$API_BASE_URL/api/profile")?.let(::parseProfile)
    }

    override val profile: Flow<GitHubProfile> = flow {
        emit(cache.get() ?: FallbackProfile.profile)
    }.flowOn(defaultDispatcher)
}

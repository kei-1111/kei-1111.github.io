package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.shared.model.GitHubProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    override val profile: Flow<GitHubProfile> = flowOf(DefaultGitHubProfile).flowOn(defaultDispatcher)
}

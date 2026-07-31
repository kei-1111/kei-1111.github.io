package io.github.kei_1111.app.core.domain.usecase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.data.repository.IssuesRepository
import io.github.kei_1111.shared.model.GitHubIssues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface GetIssuesUseCase {
    operator fun invoke(): Flow<GitHubIssues>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetIssuesUseCaseImpl(
    private val issuesRepository: IssuesRepository,
) : GetIssuesUseCase {
    override fun invoke(): Flow<GitHubIssues> =
        issuesRepository.issues
            .distinctUntilChanged()
}

package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.WorksRepository
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Work
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetWorksUseCaseTest {

    @Test
    fun forwardsTheRepositoryFlow() = runTest {
        val expected = works(id = "kei-1111.github.io")
        val useCase = GetWorksUseCaseImpl(FakeWorksRepository(flowOf(expected)))

        val actual = useCase().toList()

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun collapsesConsecutiveDuplicateEmissions() = runTest {
        val first = works(id = "first")
        val duplicate = works(id = "first")
        val second = works(id = "second")
        val useCase = GetWorksUseCaseImpl(FakeWorksRepository(flowOf(first, duplicate, second, first)))

        val actual = useCase().toList()

        assertEquals(listOf(first, second, first), actual)
    }
}

private class FakeWorksRepository(override val works: Flow<List<Work>>) : WorksRepository

private fun works(id: String) = listOf(
    Work(
        id = id,
        name = "Test",
        stack = "Kotlin",
        description = LocalizedText(ja = "テスト", en = "Test"),
        tags = persistentListOf(),
        screenshots = persistentListOf(),
    ),
)

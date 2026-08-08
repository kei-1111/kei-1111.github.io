package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.WorksRepository
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.WorkTag
import io.github.kei_1111.shared.model.Works
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

private class FakeWorksRepository(override val works: Flow<Works>) : WorksRepository

private fun works(id: String) = Works(
    items = persistentListOf(
        Work(
            id = id,
            name = "Test",
            kind = "Test App",
            period = "2024–",
            description = LocalizedText(ja = "テスト", en = "Test"),
            tags = persistentListOf(WorkTag(name = "Kotlin")),
            screenshots = persistentListOf(),
        ),
    ),
)

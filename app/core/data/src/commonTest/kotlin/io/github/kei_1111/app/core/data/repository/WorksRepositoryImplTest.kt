package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.api.works.WorksApi
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.WorkTag
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class WorksRepositoryImplTest {

    @Test
    fun emitsTheFetchedValue() = runTest {
        val expected = works()
        val repository = WorksRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            worksApi = FakeWorksApi(expected),
        )

        val actual = repository.works.first()

        assertEquals(expected, actual)
    }

    @Test
    fun throwsWhenTheFetchFails() = runTest {
        val repository = WorksRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            worksApi = FakeWorksApi(null),
        )

        assertFailsWith<IllegalStateException> {
            repository.works.first()
        }
    }

    @Test
    fun sharesOneFetchAcrossCollections() = runTest {
        val api = FakeWorksApi(works())
        val repository = WorksRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            worksApi = api,
        )

        repository.works.first()
        repository.works.first()

        assertEquals(1, api.callCount)
    }
}

private class FakeWorksApi(
    private val result: List<Work>?,
) : WorksApi {
    var callCount = 0

    override suspend fun fetchWorks(): List<Work>? {
        callCount += 1
        return result
    }
}

private fun works() = listOf(
    Work(
        id = "work",
        name = "Work",
        kind = "Android App",
        period = "2024–",
        description = LocalizedText(ja = "説明", en = "Description"),
        tags = persistentListOf(WorkTag(name = "Kotlin", accent = true)),
        screenshots = persistentListOf("https://example.com/1.webp"),
    ),
)

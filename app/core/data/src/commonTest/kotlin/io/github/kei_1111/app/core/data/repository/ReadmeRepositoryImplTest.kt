package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.api.readme.ReadmeApi
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.Readme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class ReadmeRepositoryImplTest {

    @Test
    fun emitsTheFetchedValue() = runTest {
        val expected = readme()
        val repository = ReadmeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            readmeApi = FakeReadmeApi(expected),
        )

        val actual = repository.readme.first()

        assertEquals(expected, actual)
    }

    @Test
    fun throwsWhenTheFetchFails() = runTest {
        val repository = ReadmeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            readmeApi = FakeReadmeApi(null),
        )

        assertFailsWith<IllegalStateException> {
            repository.readme.first()
        }
    }

    @Test
    fun throwsWhenTheFetchSucceedsWithAnEmptyJaReadme() = runTest {
        val repository = ReadmeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            readmeApi = FakeReadmeApi(
                Readme(
                    ja = persistentListOf(),
                    en = readme().en,
                ),
            ),
        )

        assertFailsWith<IllegalStateException> {
            repository.readme.first()
        }
    }

    @Test
    fun throwsWhenTheFetchSucceedsWithAnEmptyEnReadme() = runTest {
        val repository = ReadmeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            readmeApi = FakeReadmeApi(
                Readme(
                    ja = readme().ja,
                    en = persistentListOf(),
                ),
            ),
        )

        assertFailsWith<IllegalStateException> {
            repository.readme.first()
        }
    }

    @Test
    fun sharesOneFetchAcrossCollections() = runTest {
        val api = FakeReadmeApi(readme())
        val repository = ReadmeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            readmeApi = api,
        )

        repository.readme.first()
        repository.readme.first()

        assertEquals(1, api.callCount)
    }

    @Test
    fun refetchesAfterAnEmptyResponseInsteadOfCachingIt() = runTest {
        val api = FakeReadmeApi(Readme(ja = persistentListOf(), en = persistentListOf()))
        val repository = ReadmeRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            readmeApi = api,
        )

        assertFailsWith<IllegalStateException> {
            repository.readme.first()
        }

        api.result = readme()

        assertEquals(readme(), repository.readme.first())
        assertEquals(2, api.callCount)
    }
}

private class FakeReadmeApi(
    var result: Readme?,
) : ReadmeApi {
    var callCount = 0

    override suspend fun fetchReadme(): Readme? {
        callCount += 1
        return result
    }
}

private fun readme() = Readme(
    ja = persistentListOf(
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(MarkdownInline.PlainText("ja")),
        ),
    ),
    en = persistentListOf(
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(MarkdownInline.PlainText("en")),
        ),
    ),
)

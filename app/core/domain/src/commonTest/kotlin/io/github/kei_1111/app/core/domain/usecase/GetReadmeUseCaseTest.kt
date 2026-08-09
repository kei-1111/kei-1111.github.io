package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.ReadmeRepository
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.Readme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetReadmeUseCaseTest {

    @Test
    fun forwardsTheRepositoryFlow() = runTest {
        val expected = readme(jaText = "ja", enText = "en")
        val useCase = GetReadmeUseCaseImpl(FakeReadmeRepository(flowOf(expected)))

        val actual = useCase().toList()

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun collapsesConsecutiveDuplicateEmissions() = runTest {
        val first = readme(jaText = "first-ja", enText = "first-en")
        val duplicate = readme(jaText = "first-ja", enText = "first-en")
        val second = readme(jaText = "second-ja", enText = "second-en")
        val useCase = GetReadmeUseCaseImpl(FakeReadmeRepository(flowOf(first, duplicate, second, first)))

        val actual = useCase().toList()

        assertEquals(listOf(first, second, first), actual)
    }
}

private class FakeReadmeRepository(override val readme: Flow<Readme>) : ReadmeRepository

private fun readme(
    jaText: String,
    enText: String,
) = Readme(
    ja = persistentListOf(
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(MarkdownInline.PlainText(jaText)),
        ),
    ),
    en = persistentListOf(
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(MarkdownInline.PlainText(enText)),
        ),
    ),
)

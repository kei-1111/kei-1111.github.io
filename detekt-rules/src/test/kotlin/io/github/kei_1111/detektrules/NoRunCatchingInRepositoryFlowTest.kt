package io.github.kei_1111.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NoRunCatchingInRepositoryFlowTest {
    @Test
    fun reportsRunCatchingInsideFlowBuilder() {
        val findings = NoRunCatchingInRepositoryFlow(Config.empty).lint(
            """
            fun repositoryFlow() = flow {
                runCatching {
                    api.fetch()
                }
            }
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun allowsFlowBuilderWithoutRunCatching() {
        val findings = NoRunCatchingInRepositoryFlow(Config.empty).lint(
            """
            fun repositoryFlow() = flow {
                emit(api.fetch())
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }
}

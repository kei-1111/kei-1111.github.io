package io.github.kei_1111.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewModelFlowCollectionNeedsAsResultTest {
    @Test
    fun reportsCollectWithoutAsResult() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            fun observe() {
                getProfileUseCase().collect { }
            }
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun reportsLaunchInWithoutAsResult() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            fun observe() {
                getProfileUseCase().launchIn(viewModelScope)
            }
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun allowsCollectAfterAsResult() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            fun observe() {
                getProfileUseCase().asResult().collect { }
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun allowsFirstAfterAsResult() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            fun observe() {
                getProfileUseCase().asResult().first { it !is Result.Loading }
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun allowsLaunchInWhenItsReceiverIsAsResult() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            fun prefetchAsResult() {
                asResult().launchIn(viewModelScope)
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun allowsCollectingInteractionLogEntries() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            fun observe() {
                interactionLog.entries.collect { }
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun allowsCollectingDerivedViewModelState() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            fun observe() {
                viewModelState.map { it }.distinctUntilChanged().collect { }
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun reportsCollectWhenOnlyCombinedInputUsesAsResult() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            fun observe() {
                combine(getProfileUseCase(), getWorksUseCase().asResult()).collect { }
            }
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun reportsCollectFromAliasedUseCasePropertyWithoutAsResult() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            class FooViewModel(private val profileLoader: GetProfileUseCase) {
                suspend fun observe() {
                    profileLoader().collect { }
                }
            }
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun allowsCollectFromAliasedUseCasePropertyAfterAsResult() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            class FooViewModel(private val profileLoader: GetProfileUseCase) {
                suspend fun observe() {
                    profileLoader().asResult().collect { }
                }
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun allowsCollectWhenMergedFlowUsesAsResult() {
        val findings = ViewModelFlowCollectionNeedsAsResult(Config.empty).lint(
            """
            fun observe() {
                merge(getProfileUseCase(), getWorksUseCase()).asResult().collect { }
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }
}

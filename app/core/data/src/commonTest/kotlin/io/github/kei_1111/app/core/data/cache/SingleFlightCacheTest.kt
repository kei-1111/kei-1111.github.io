package io.github.kei_1111.app.core.data.cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SingleFlightCacheTest {

    @Test
    fun sharesOneInFlightFetchAcrossConcurrentGets() = runTest {
        var fetchCount = 0
        val fetchStarted = CompletableDeferred<Unit>()
        val releaseFetch = CompletableDeferred<Unit>()
        val cache = SingleFlightCache(StandardTestDispatcher(testScheduler)) {
            fetchCount += 1
            fetchStarted.complete(Unit)
            releaseFetch.await()
            "value"
        }

        val first = async { cache.get() }
        val second = async { cache.get() }
        runCurrent()
        fetchStarted.await()
        releaseFetch.complete(Unit)
        val actual = awaitAll(first, second)

        assertEquals(listOf("value", "value"), actual)
        assertEquals(1, fetchCount)
    }

    @Test
    fun reusesCachedValueWithoutRefetching() = runTest {
        var fetchCount = 0
        val cache = SingleFlightCache(StandardTestDispatcher(testScheduler)) {
            fetchCount += 1
            "value"
        }

        val first = cache.get()
        val second = cache.get()

        assertEquals("value", first)
        assertEquals("value", second)
        assertEquals(1, fetchCount)
    }

    @Test
    fun retriesAfterNullResultInsteadOfCachingFailure() = runTest {
        var fetchCount = 0
        val cache = SingleFlightCache<String>(StandardTestDispatcher(testScheduler)) {
            fetchCount += 1
            if (fetchCount == 1) null else "value"
        }

        val first = cache.get()
        val second = cache.get()

        assertNull(first)
        assertEquals("value", second)
        assertEquals(2, fetchCount)
    }

    @Test
    fun treatsThrownFetchFailureAsNullAndRetries() = runTest {
        var fetchCount = 0
        val cache = SingleFlightCache<String>(StandardTestDispatcher(testScheduler)) {
            fetchCount += 1
            if (fetchCount == 1) error("fetch failed")
            "value"
        }

        val first = cache.get()
        val second = cache.get()

        assertNull(first)
        assertEquals("value", second)
        assertEquals(2, fetchCount)
    }

    @Test
    fun keepsFetchAliveWhenTheWaitingCallerIsCancelled() = runTest {
        var fetchCount = 0
        val fetchStarted = CompletableDeferred<Unit>()
        val releaseFetch = CompletableDeferred<Unit>()
        val cache = SingleFlightCache(StandardTestDispatcher(testScheduler)) {
            fetchCount += 1
            fetchStarted.complete(Unit)
            releaseFetch.await()
            "value"
        }

        val job = launch { cache.get() }
        runCurrent()
        fetchStarted.await()
        job.cancelAndJoin()
        releaseFetch.complete(Unit)
        val actual = cache.get()

        assertEquals("value", actual)
        assertEquals(1, fetchCount)
    }
}

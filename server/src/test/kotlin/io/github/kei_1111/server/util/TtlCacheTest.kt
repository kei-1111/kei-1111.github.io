package io.github.kei_1111.server.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

private const val TTL_MILLIS = 10_000L
private const val RETRY_INTERVAL_MILLIS = 60_000L

class TtlCacheTest {

    private val timeSource = TestTimeSource()

    private fun cache() = TtlCache<String>(TTL_MILLIS, RETRY_INTERVAL_MILLIS, timeSource)

    @Test
    fun returnsCachedValueWithinTtlWithoutRefetching() = runBlocking {
        val cache = cache()
        val fetchCount = AtomicInteger()

        val first = cache.get {
            fetchCount.incrementAndGet()
            "value"
        }
        timeSource += (TTL_MILLIS - 1).milliseconds
        val second = cache.get {
            fetchCount.incrementAndGet()
            "value"
        }

        assertEquals("value", first)
        assertEquals("value", second)
        assertEquals(1, fetchCount.get())
    }

    @Test
    fun refetchesAfterTtlExpires() = runBlocking {
        val cache = cache()
        val fetchCount = AtomicInteger()

        val first = cache.get { "value${fetchCount.incrementAndGet()}" }
        timeSource += TTL_MILLIS.milliseconds
        val second = cache.get { "value${fetchCount.incrementAndGet()}" }

        assertEquals("value1", first)
        assertEquals("value2", second)
        assertEquals(2, fetchCount.get())
    }

    @Test
    fun refetchesAfterTtlEvenWhenTheRetryIntervalIsLongerThanTheTtl() = runBlocking {
        // 抑止は失敗にのみ効くので、成功後の TTL 失効時は retryInterval の長さに関わらず再取得する。
        val cache = cache()
        val fetchCount = AtomicInteger()

        val first = cache.get { "value${fetchCount.incrementAndGet()}" }
        timeSource += TTL_MILLIS.milliseconds
        val second = cache.get { "value${fetchCount.incrementAndGet()}" }

        assertEquals("value1", first)
        assertEquals("value2", second)
        assertEquals(2, fetchCount.get())
    }

    @Test
    fun coalescesConcurrentGetsIntoASingleFetch() = runBlocking {
        val cache = cache()
        val fetchCount = AtomicInteger()

        val results = coroutineScope {
            List(16) {
                async {
                    cache.get {
                        fetchCount.incrementAndGet()
                        delay(20)
                        "value"
                    }
                }
            }.awaitAll()
        }

        assertEquals(List(16) { "value" }, results)
        assertEquals(1, fetchCount.get())
    }

    @Test
    fun servesStaleValueWhenRefetchFails() = runBlocking {
        val cache = cache()

        val fresh = cache.get { "fresh" }
        timeSource += TTL_MILLIS.milliseconds
        val stale = cache.get { null }

        assertEquals("fresh", fresh)
        assertEquals("fresh", stale)
    }

    @Test
    fun suppressesRetriesWithinTheRetryIntervalAfterAFailure() = runBlocking {
        val cache = cache()
        val fetchCount = AtomicInteger()

        val first = cache.get {
            fetchCount.incrementAndGet()
            null
        }
        timeSource += (RETRY_INTERVAL_MILLIS - 1).milliseconds
        val second = cache.get {
            fetchCount.incrementAndGet()
            null
        }

        assertNull(first)
        assertNull(second)
        // 失敗直後は retryInterval の間だけ再取得を抑止するため、fetch は1回しか走らない。
        assertEquals(1, fetchCount.get())
    }

    @Test
    fun retriesAfterTheRetryIntervalElapses() = runBlocking {
        val cache = cache()
        val fetchCount = AtomicInteger()

        val failed = cache.get {
            fetchCount.incrementAndGet()
            null
        }
        timeSource += RETRY_INTERVAL_MILLIS.milliseconds
        val recovered = cache.get {
            fetchCount.incrementAndGet()
            "value"
        }

        assertNull(failed)
        assertEquals("value", recovered)
        assertEquals(2, fetchCount.get())
    }

    @Test
    fun suppressedRetryStillServesTheStaleValue() = runBlocking {
        val cache = cache()
        val fetchCount = AtomicInteger()

        val fresh = cache.get {
            fetchCount.incrementAndGet()
            "fresh"
        }
        timeSource += TTL_MILLIS.milliseconds
        val stale = cache.get {
            fetchCount.incrementAndGet()
            null
        }
        val suppressed = cache.get {
            fetchCount.incrementAndGet()
            null
        }

        assertEquals("fresh", fresh)
        assertEquals("fresh", stale)
        assertEquals("fresh", suppressed)
        // 初回成功 + 失効後の失敗のみ。直後の3回目は retryInterval 内なので抑止され、stale が返る。
        assertEquals(2, fetchCount.get())
    }
}

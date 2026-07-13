package io.github.kei_1111.server.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * 成功値のみを TTL 付きで保持するキャッシュ。Mutex で再取得を単一飛行化し、失効後の再取得に失敗した場合は
 * 直近の成功値を返す(stale-if-error)。失敗直後は retryInterval の間だけ再取得を抑止し、障害中に
 * 待機リクエスト数ぶんの再試行が積み上がるのを防ぐ。
 */
class TtlCache<T : Any>(
    private val ttlMillis: Long,
    private val retryIntervalMillis: Long = DEFAULT_RETRY_INTERVAL_MILLIS,
) {
    private val mutex = Mutex()
    private var cached: T? = null
    private var cachedAt: TimeMark? = null
    private var lastAttemptAt: TimeMark? = null

    suspend fun get(fetch: suspend () -> T?): T? = mutex.withLock {
        val held = cached
        when {
            held != null && isWithin(cachedAt, ttlMillis) -> held
            // 直近の取得試行から retryInterval 以内なら再試行せず、あれば stale を返す。
            isWithin(lastAttemptAt, retryIntervalMillis) -> held
            else -> {
                val fresh = fetch()
                // 経過時間は fetch 完了後に計測する(取得時間を TTL に含めない)。
                val completedAt = TimeSource.Monotonic.markNow()
                lastAttemptAt = completedAt
                if (fresh != null) {
                    cached = fresh
                    cachedAt = completedAt
                }
                fresh ?: held
            }
        }
    }

    private fun isWithin(mark: TimeMark?, windowMillis: Long): Boolean =
        mark != null && mark.elapsedNow().inWholeMilliseconds < windowMillis

    companion object {
        private const val DEFAULT_RETRY_INTERVAL_MILLIS = 60_000L
    }
}

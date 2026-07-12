package io.github.kei_1111.server.github

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 成功値のみを TTL 付きで保持するキャッシュ。Mutex で再取得を単一飛行化し、
 * 失効後の再取得に失敗した場合は直近の成功値を返す(stale-if-error)。
 */
class TtlCache<T : Any>(private val ttlMillis: Long) {
    private val mutex = Mutex()
    private var cached: T? = null
    private var fetchedAtMillis = 0L

    suspend fun get(fetch: suspend () -> T?): T? = mutex.withLock {
        val now = System.currentTimeMillis()
        val held = cached
        if (held != null && now - fetchedAtMillis < ttlMillis) {
            held
        } else {
            val fresh = fetch()
            if (fresh != null) {
                cached = fresh
                fetchedAtMillis = now
            }
            fresh ?: held
        }
    }
}

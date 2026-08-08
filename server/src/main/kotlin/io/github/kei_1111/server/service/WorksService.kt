package io.github.kei_1111.server.service

import io.github.kei_1111.server.content.DefaultWorks
import io.github.kei_1111.shared.model.Works

/** works は GitHub API に依存しない静的コンテンツのため、キャッシュもフォールバックも持たない。 */
class WorksService {
    fun getWorks(): Works = DefaultWorks
}

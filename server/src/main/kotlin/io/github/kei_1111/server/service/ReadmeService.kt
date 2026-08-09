package io.github.kei_1111.server.service

import io.github.kei_1111.server.content.DefaultReadme
import io.github.kei_1111.shared.model.Readme

/** readme は GitHub API に依存しない静的コンテンツのため、キャッシュもフォールバックも持たない。 */
class ReadmeService {
    fun getReadme(): Readme = DefaultReadme
}

package io.github.kei_1111.server.content

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * DefaultWorks の相対アセットパスは wasm クライアントの同梱リソースを指す（解決はクライアントの
 * resolveAssetUrl）。リネーム・削除がプレースホルダ表示へサイレントに退行しないよう、
 * 配布物のソースディレクトリと突き合わせて固定する。
 */
class WorksAssetPathsTest {

    @Test
    fun relativeAssetPathsPointToBundledClientResources() {
        val repoRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }
        val clientResources = File(repoRoot, "app/webApp/src/wasmJsMain/resources")

        val relativePaths = DefaultWorks.items
            .flatMap { work -> listOfNotNull(work.iconUrl) + work.screenshots }
            .filterNot { it.startsWith("http") }

        assertTrue(relativePaths.isNotEmpty(), "expected bundled asset paths in DefaultWorks")
        val missing = relativePaths.filterNot { File(clientResources, it).isFile }
        assertTrue(missing.isEmpty(), "missing client resources for: $missing")
    }
}

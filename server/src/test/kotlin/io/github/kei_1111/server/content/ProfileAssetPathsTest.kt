package io.github.kei_1111.server.content

import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * DefaultGitHubProfile の相対アイコンパスは wasm クライアントの同梱リソースを指す（解決はクライアントの
 * resolveAssetUrl）。リネーム・削除が既定画像表示へサイレントに退行しないよう、
 * 配布物のソースディレクトリと突き合わせて固定する。
 */
class ProfileAssetPathsTest {

    @Test
    fun iconUrlPointsToBundledClientResource() {
        val repoRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }
        val clientResources = File(repoRoot, "app/webApp/src/wasmJsMain/resources")

        val iconUrl = assertNotNull(DefaultGitHubProfile.iconUrl)
        assertTrue(!iconUrl.startsWith("http"), "expected an app-relative path, got $iconUrl")
        assertTrue(File(clientResources, iconUrl).isFile, "missing client resource for: $iconUrl")
    }
}

package io.github.kei_1111

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private const val CompileSdk = 36
private const val MinSdk = 24

internal fun Project.configureKmpWasm(
    kotlinMultiplatformExtension: KotlinMultiplatformExtension,
) {
    kotlinMultiplatformExtension.apply {
        wasmJs {
            browser()
            binaries.executable()
        }

        // IDE で commonMain の Compose Preview を描画するための Android ターゲット。
        // プレビューは Android の描画基盤 (layoutlib) に依存しており、
        // wasm 単独では表示できない。配布物はあくまで wasmJs（composeApp は wasm のみ）。
        (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("androidLibrary") {
            namespace = "io.github.kei_1111" +
                project.path.replace(":", ".").replace("-", "_")
            compileSdk = CompileSdk
            minSdk = MinSdk
        }
    }
}

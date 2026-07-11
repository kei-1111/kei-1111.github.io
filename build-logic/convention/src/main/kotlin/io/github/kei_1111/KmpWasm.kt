package io.github.kei_1111

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
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
        }

        // IDE で commonMain の Compose Preview を描画するための Android ターゲット。
        // プレビューは Android の描画基盤 (layoutlib) に依存しており、
        // wasm 単独では表示できない。配布物はあくまで wasmJs（composeApp は wasm のみ）。
        // 拡張名 "androidLibrary" は AGP 9.1.0-alpha09 で非推奨化され、"android" に改名された
        // (AGP 8.12.0 で導入)。KotlinMultiplatformExtension は KotlinProjectExtension 経由で
        // ExtensionAware を実装しているため、追加のキャストなしで extensions にアクセスできる。
        extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
            namespace = "io.github.kei_1111" +
                project.path.replace(":", ".").replace("-", "_")
            compileSdk = CompileSdk
            minSdk = MinSdk
        }
    }
}

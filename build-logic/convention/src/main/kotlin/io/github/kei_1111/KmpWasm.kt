package io.github.kei_1111

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKmpWasm(
    kotlinMultiplatformExtension: KotlinMultiplatformExtension,
) {
    kotlinMultiplatformExtension.apply {
        wasmJs {
            browser()
            binaries.executable()
        }
    }
}

package io.github.kei_1111

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project

internal fun Project.setupDetekt(
    extension: DetektExtension,
) {
    extension.apply {
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        buildUponDefaultConfig = true
        source.setFrom(files("src"))
        autoCorrect = true
    }
}

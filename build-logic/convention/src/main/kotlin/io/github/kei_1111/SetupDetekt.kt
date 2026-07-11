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
        // CI では整形結果が捨てられる上、同一 Gradle 呼び出しに同居するコンパイルタスクと
        // 並列実行されるため、書き換え中のソースを読むレースの窓を塞ぐ目的で無効化する
        autoCorrect = providers.environmentVariable("CI").orNull == null
        parallel = true
    }
}

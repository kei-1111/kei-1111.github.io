plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
    // 走査対象 (app/feature・template・app/core/mvi のソース) はこのモジュールのタスク入力に
    // 含まれないため、スキップ・キャッシュ再利用は違反を素通しする。常に実行する。
    outputs.upToDateWhen { false }
    outputs.doNotCacheIf("Konsist scans sources outside this module's task inputs") { true }
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.konsist)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

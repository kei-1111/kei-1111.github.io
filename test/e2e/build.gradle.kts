plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
    // 配信済みアプリが前提のため、check / build の巻き添えでは走らせず -PbaseUrl 指定時だけ実行する
    val baseUrl = providers.gradleProperty("baseUrl")
    onlyIf { baseUrl.isPresent }
    baseUrl.orNull?.let { systemProperty("baseUrl", it) }
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// Playwright が使う Chromium を取得する（初回のみ実行すればよい）
tasks.register<JavaExec>("installPlaywright") {
    description = "Downloads the Chromium build used by the Playwright E2E tests."
    group = "verification"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.microsoft.playwright.CLI")
    args("install", "chromium")
}

dependencies {
    testImplementation(projects.test.tags)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.playwright)
    testRuntimeOnly(libs.junit.platform.launcher)
}

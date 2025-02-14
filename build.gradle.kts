import io.gitlab.arturbosch.detekt.Detekt

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlinMultiplatform) apply false
}

val detektFormatting = libs.detekt.formatting
val detektCompose = libs.detekt.compose

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        detektPlugins(detektFormatting)
        detektPlugins(detektCompose)
    }

    detekt {
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        buildUponDefaultConfig = true

        source.setFrom(files("src"))
        autoCorrect = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
    }
}

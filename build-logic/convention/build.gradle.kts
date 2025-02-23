plugins {
    `kotlin-dsl`
}

group = "io.github.kei_1111.build_logic.convention"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencies {
    compileOnly(libs.android.gradle)
    compileOnly(libs.compose.jb.gradle)
    compileOnly(libs.detekt.gradle)
    compileOnly(libs.kotlin.gradle)
}

gradlePlugin {
    plugins {
        register("detekt") {
            id = libs.plugins.kei1111.detekt.get().pluginId
            implementationClass = "DetektPlugin"
        }

        register("kmpFeature") {
            id = libs.plugins.kei1111.kmp.feature.get().pluginId
            implementationClass = "KmpFeaturePlugin"
        }

        register("kmpWasm") {
            id = libs.plugins.kei1111.kmp.wasm.get().pluginId
            implementationClass = "KmpWasmPlugin"
        }
    }
}

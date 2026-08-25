plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kei1111.kmp.wasm)
    alias(libs.plugins.kei1111.kmp.host.test)
    alias(libs.plugins.kei1111.metro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.app.core.common)
            implementation(projects.shared.model)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.mock)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

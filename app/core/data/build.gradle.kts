plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kei1111.kmp.wasm)
    alias(libs.plugins.kei1111.metro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.app.core.api)
            implementation(projects.app.core.common)
            implementation(projects.app.core.local)
            implementation(projects.shared.model)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

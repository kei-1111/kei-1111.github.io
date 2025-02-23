plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kei1111.kmp.wasm)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation)
        }
    }
}
plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kei1111.kmp.wasm)
    alias(libs.plugins.kei1111.metro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.okio)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

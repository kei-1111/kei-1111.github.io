plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kei1111.kmp.wasm)
    alias(libs.plugins.kei1111.cmp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.navigation3.runtime)
            implementation(libs.navigation3.ui)
            implementation(libs.compose.runtime)
            implementation(libs.compose.animation)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core)
            implementation(projects.app.core.designsystem)
        }
    }
}

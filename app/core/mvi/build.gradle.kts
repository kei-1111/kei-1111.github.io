plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kei1111.kmp.wasm)
    alias(libs.plugins.kei1111.kmp.host.test)
    alias(libs.plugins.kei1111.cmp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.app.core.common)
            implementation(libs.compose.runtime)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(projects.app.core.testing)

            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

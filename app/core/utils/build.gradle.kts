plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kei1111.kmp.wasm)
    alias(libs.plugins.kei1111.cmp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            // ResizeCursor.kt が公開APIで PointerIcon を返すため api で公開する
            api(libs.compose.ui)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

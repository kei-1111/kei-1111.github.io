plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kei1111.kmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.materialIconsExtended)
        }
    }
}
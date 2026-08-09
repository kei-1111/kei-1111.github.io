plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kei1111.kmp.wasm)
    alias(libs.plugins.kei1111.kmp.host.test)
    alias(libs.plugins.kei1111.cmp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // LinkServiceType など、見た目の定義がキーに取るモデル型のため
            implementation(projects.shared.model)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)

            implementation(projects.app.core.utils)

            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            // coil-network-ktor3 は古い ktor-client-core を推移で引くため、カタログ版を明示して揃える
            implementation(libs.ktor.client.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

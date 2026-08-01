import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget

plugins {
    alias(libs.plugins.kei1111.detekt)
    alias(libs.plugins.kei1111.kmp.wasm)
    alias(libs.plugins.kei1111.metro)
    alias(libs.plugins.serialization)
}

kotlin {
    // Repository のユニットテスト (commonTest) をローカル JVM で実行するためのホストテスト。
    // feature モジュールには kei_1111.kmp.feature が同じ設定を適用する。
    extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
        withHostTestBuilder {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.app.core.common)
            implementation(projects.shared.model)
            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.okio)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

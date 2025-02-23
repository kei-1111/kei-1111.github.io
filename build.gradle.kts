plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.serialization) apply false

//    kei_1111
    alias(libs.plugins.kei1111.detekt) apply false
    alias(libs.plugins.kei1111.kmp.wasm) apply false
    alias(libs.plugins.kei1111.kmp.feature) apply false
}

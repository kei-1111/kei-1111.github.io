package io.github.kei_1111

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * ユニットテスト (commonTest) をローカル JVM で実行するホストテストの受け皿。
 * 全モジュール共通の kei_1111.kmp.wasm 側には置かない — テストを持たないモジュールにまで
 * テスト用コンパイルが波及するため、テストを持つ core モジュールだけが適用する
 * (feature モジュールには kei_1111.kmp.feature が同じ設定を適用する)。
 */
class KmpHostTestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.configure<KotlinMultiplatformExtension> {
            extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                withHostTestBuilder {}
            }
        }
    }
}

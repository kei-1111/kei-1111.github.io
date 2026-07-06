import io.github.kei_1111.configureKmpWasm
import io.github.kei_1111.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpWasmPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.kotlin.multiplatform.library")
            apply(plugin = "org.jetbrains.compose")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")
            apply(plugin = "org.jetbrains.kotlin.multiplatform")

            extensions.configure<KotlinMultiplatformExtension> {
                configureKmpWasm(this)
            }

            // commonMain の @Preview を IDE (layoutlib) で描画するためのツーリング依存。
            dependencies.add(
                "androidRuntimeClasspath",
                libs.findLibrary("compose.ui.tooling").get().get(),
            )
        }
    }
}

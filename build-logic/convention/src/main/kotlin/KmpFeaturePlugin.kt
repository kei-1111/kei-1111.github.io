import io.github.kei_1111.compose
import io.github.kei_1111.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "kei_1111.kmp.wasm")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

            extensions.configure<KotlinMultiplatformExtension> {
                with(sourceSets) {
                    getByName("commonMain").apply {
                        dependencies {
                            implementation(project(":core:common"))
                            implementation(project(":core:designsystem"))

                            implementation(compose.dependencies.runtime)
                            implementation(compose.dependencies.foundation)
                            implementation(compose.dependencies.material3)
                            implementation(compose.dependencies.ui)
                            implementation(compose.dependencies.components.resources)
                            implementation(compose.dependencies.components.uiToolingPreview)
                            implementation(libs.findLibrary("kotlinx.serialization.json").get())
                            implementation(libs.findLibrary("navigation.compose").get())
                        }
                    }
                }
            }
        }
    }
}
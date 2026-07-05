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

                            implementation(libs.findLibrary("compose.runtime").get())
                            implementation(libs.findLibrary("compose.foundation").get())
                            implementation(libs.findLibrary("compose.material3").get())
                            implementation(libs.findLibrary("compose.ui").get())
                            implementation(libs.findLibrary("compose.components.resources").get())
                            implementation(libs.findLibrary("compose.ui.tooling.preview").get())
                            implementation(libs.findLibrary("kotlinx.serialization.json").get())
                            implementation(libs.findLibrary("navigation.compose").get())
                        }
                    }
                }
            }
        }
    }
}
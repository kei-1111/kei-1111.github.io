import io.github.kei_1111.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class CmpPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.compose")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")

            // commonMain の @Preview を IDE (layoutlib) で描画するためのツーリング依存。
            // Compose を使うモジュールに Android target（プレビュー用）があれば @Preview 描画に必要になる。
            // feature 以外（designsystem 等）に将来 @Preview を置いても壊れないよう、kmp.feature ではなく
            // cmp 側で配線し、Android target が存在するモジュールにのみ遅延適用する。
            // wasm のみで Android target を持たない composeApp はこのガードで自動的にスキップされる。
            pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
                dependencies.addProvider("androidRuntimeClasspath", libs.findLibrary("compose.ui.tooling").get())
            }
        }
    }
}

package primitive
import me.matsumo.fanbox.configureDetekt
import me.matsumo.fanbox.library
import me.matsumo.fanbox.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Kotlin と Compose の静的解析をモジュールへ設定する。 */
class DetektPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("dev.detekt")

            configureDetekt()

            dependencies {
                "detektPlugins"(libs.library("detekt-ktlint-wrapper"))
                "detektPlugins"(libs.library("compose-rules-detekt"))
            }
        }
    }
}

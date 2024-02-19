package primitive
import me.matsumo.fanbox.configureDetekt
import me.matsumo.fanbox.library
import me.matsumo.fanbox.libsMain
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class DetektPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            configureDetekt()

            dependencies {
                "detektPlugins"(libsMain.library("detekt-formatting"))
                "detektPlugins"(libsMain.library("twitter-compose-rule"))
            }
        }
    }
}

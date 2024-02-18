package primitive
import me.matsumo.fanbox.bundle
import me.matsumo.fanbox.implementation
import me.matsumo.fanbox.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KmpResources : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("dev.icerock.mobile.multiplatform-resources")
            }

            dependencies {
                implementation(libs.bundle("resources"))
            }
        }
    }
}

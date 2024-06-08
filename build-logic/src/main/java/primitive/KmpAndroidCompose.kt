package primitive
import me.matsumo.fanbox.android
import me.matsumo.fanbox.androidTestImplementation
import me.matsumo.fanbox.debugImplementation
import me.matsumo.fanbox.implementation
import me.matsumo.fanbox.library
import me.matsumo.fanbox.libsMain
import me.matsumo.fanbox.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KmpAndroidCompose : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            android {
                buildFeatures.compose = true
            }

            dependencies {
                val bom = libsMain.library("androidx-compose-bom")

                implementation(project.dependencies.platform(bom))
                implementation(libsMain.library("androidx-compose-ui-tooling-preview"))
                debugImplementation(libsMain.library("androidx-compose-ui-tooling"))
                androidTestImplementation(project.dependencies.platform(bom))
            }
        }
    }
}

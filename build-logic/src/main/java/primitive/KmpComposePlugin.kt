package primitive

import org.gradle.api.Plugin
import org.gradle.api.Project

/** Compose Multiplatform と Compose compiler を設定する。 */
class KmpComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }
        }
    }
}

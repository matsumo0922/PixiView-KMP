
import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpIosPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // apply("com.louiscad.complete-kotlin")
            }

            kotlin {
                applyDefaultHierarchyTemplate()

                listOf(
                    iosX64(),
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = "ComposeApp"
                        isStatic = true
                    }
                }
            }
        }
    }
}

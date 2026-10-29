package primitive
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Kotlin Multiplatform の iOS target を設定する。 */
class KmpIosPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            kotlin {
                listOf(
                    iosArm64(),
                    iosSimulatorArm64(),
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = "shared"
                        isStatic = true
                    }
                }

                sourceSets.named { it.lowercase().startsWith("ios") }.configureEach {
                    languageSettings {
                        optIn("kotlinx.cinterop.ExperimentalForeignApi")
                    }
                }
            }
        }
    }
}

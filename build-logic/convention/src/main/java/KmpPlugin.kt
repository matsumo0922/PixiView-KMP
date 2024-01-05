
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
            }
            tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
                kotlinOptions.jvmTarget = "17"
            }
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink>().configureEach {
                notCompatibleWithConfigurationCache("Configuration chache not supported for a system property read at configuration time")
            }
        }
    }
}

fun Project.kotlin(action: KotlinMultiplatformExtension.() -> Unit) {
    extensions.configure(action)
}

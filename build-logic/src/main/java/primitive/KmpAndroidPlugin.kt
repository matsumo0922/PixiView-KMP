package primitive
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import me.matsumo.fanbox.libs
import me.matsumo.fanbox.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

@Suppress("unused")
class KmpAndroidPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
                kotlin {
                    targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
                        compileSdk = libs.version("compileSdk").toInt()
                        minSdk = libs.version("minSdk").toInt()

                        androidResources {
                            enable = true
                        }

                        compilerOptions {
                            jvmTarget.set(JvmTarget.JVM_21)
                        }
                    }
                }
            }
        }
    }
}

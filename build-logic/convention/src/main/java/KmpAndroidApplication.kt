
import me.matsumo.fanbox.androidApplication
import me.matsumo.fanbox.androidTestImplementation
import me.matsumo.fanbox.debugImplementation
import me.matsumo.fanbox.implementation
import me.matsumo.fanbox.library
import me.matsumo.fanbox.libs
import me.matsumo.fanbox.setupAndroid
import me.matsumo.fanbox.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KmpAndroidApplication : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("kotlin-parcelize")
                apply("kotlinx-serialization")
                apply("project-report")
                apply("com.mikepenz.aboutlibraries.plugin")
                apply("com.codingfeline.buildkonfig")
                apply("com.google.devtools.ksp")
                // apply("com.google.gms.google-services")
                // apply("com.google.android.gms.oss-licenses-plugin")
                apply("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
            }

            androidApplication {
                setupAndroid()

                compileSdk = libs.version("compileSdk").toInt()

                defaultConfig {
                    applicationId = "me.matsumo.fanbox"

                    versionName = libs.version("versionName")
                    versionCode = libs.version("versionCode").toInt()
                }

                buildFeatures {
                    viewBinding = true
                }

                packaging {
                    resources.excludes.addAll(
                        listOf(
                            "LICENSE",
                            "LICENSE.txt",
                            "NOTICE",
                            "asm-license.txt",
                            "cglib-license.txt",
                            "mozilla/public-suffix-list.txt",
                        )
                    )
                }
            }
        }
    }
}

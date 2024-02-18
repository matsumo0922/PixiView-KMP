package primitive
import me.matsumo.fanbox.androidApplication
import me.matsumo.fanbox.libsMain
import me.matsumo.fanbox.setupAndroid
import me.matsumo.fanbox.version
import org.gradle.api.Plugin
import org.gradle.api.Project

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

                compileSdk = libsMain.version("compileSdk").toInt()

                defaultConfig {
                    applicationId = "caios.android.fanbox"

                    versionName = libsMain.version("versionName")
                    versionCode = libsMain.version("versionCode").toInt()
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

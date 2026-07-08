package primitive

import me.matsumo.fanbox.androidApplication
import me.matsumo.fanbox.libs
import me.matsumo.fanbox.setupAndroid
import me.matsumo.fanbox.version
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("kotlin-parcelize")
                apply("kotlinx-serialization")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("project-report")
                apply("com.google.gms.google-services")
                apply("com.google.firebase.crashlytics")
                apply("com.google.devtools.ksp")
                apply("com.mikepenz.aboutlibraries.plugin")
            }

            androidApplication {
                setupAndroid()

                compileSdk = libs.version("compileSdk").toInt()
                defaultConfig.targetSdk = libs.version("targetSdk").toInt()
                buildFeatures.compose = true
                buildFeatures.buildConfig = true
                buildFeatures.resValues = true
                buildFeatures.viewBinding = true

                defaultConfig {
                    applicationId = "caios.android.fanbox"

                    versionName = libs.version("versionName")
                    versionCode = libs.version("versionCode").toInt()
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
                        ),
                    )
                }
            }
        }
    }
}

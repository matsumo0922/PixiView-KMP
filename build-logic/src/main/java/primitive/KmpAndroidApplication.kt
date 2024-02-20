package primitive
import me.matsumo.fanbox.androidApplication
import me.matsumo.fanbox.bundle
import me.matsumo.fanbox.implementation
import me.matsumo.fanbox.library
import me.matsumo.fanbox.libsMain
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
                apply("com.google.firebase.crashlytics")
                apply("com.google.devtools.ksp")
                // apply("com.google.gms.google-services")
                apply("com.mikepenz.aboutlibraries.plugin")
                apply("com.codingfeline.buildkonfig")
            }

            androidApplication {
                setupAndroid()

                compileSdk = libsMain.version("compileSdk").toInt()
                defaultConfig.targetSdk = libsMain.version("targetSdk").toInt()
                buildFeatures.viewBinding = true

                defaultConfig {
                    applicationId = "caios.android.fanbox"

                    versionName = libsMain.version("versionName")
                    versionCode = libsMain.version("versionCode").toInt()
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

            dependencies {
                val bom = libsMain.library("firebase-bom")

                implementation(platform(bom))
                implementation(libsMain.bundle("firebase"))
            }
        }
    }
}

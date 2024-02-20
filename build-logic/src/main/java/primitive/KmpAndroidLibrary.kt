package primitive
import com.android.build.gradle.LibraryExtension
import me.matsumo.fanbox.androidLibrary
import me.matsumo.fanbox.libsMain
import me.matsumo.fanbox.setupAndroid
import me.matsumo.fanbox.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class KmpAndroidLibrary: Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("kotlin-parcelize")
                apply("kotlinx-serialization")
                apply("project-report")
                apply("com.google.devtools.ksp")
            }

            androidLibrary {
                setupAndroid()
            }

            extensions.configure<LibraryExtension> {
                compileSdk = libsMain.version("compileSdk").toInt()
                defaultConfig.targetSdk = libsMain.version("targetSdk").toInt()
                buildFeatures.viewBinding = true
            }
        }
    }
}

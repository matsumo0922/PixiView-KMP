
import com.android.build.gradle.LibraryExtension
import me.matsumo.fanbox.androidLibrary
import me.matsumo.fanbox.androidTestImplementation
import me.matsumo.fanbox.debugImplementation
import me.matsumo.fanbox.implementation
import me.matsumo.fanbox.library
import me.matsumo.fanbox.libs
import me.matsumo.fanbox.setupAndroid
import me.matsumo.fanbox.version
import org.apache.tools.ant.taskdefs.Java
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

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
                compileSdk = libs.version("compileSdk").toInt()
                defaultConfig.targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
                buildFeatures.viewBinding = true
            }
        }
    }
}

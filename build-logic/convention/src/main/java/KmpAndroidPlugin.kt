
import me.matsumo.fanbox.android
import me.matsumo.fanbox.setupAndroid
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get

@Suppress("unused")
class KmpAndroidPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            kotlin {
                androidTarget {
                    compilations.all {
                        kotlinOptions {
                            jvmTarget = JavaVersion.VERSION_17.toString()
                        }
                    }
                }
            }

            android {
                sourceSets {
                    getByName("main") {
                        manifest.srcFile("src/androidMain/AndroidManifest.xml")
                        res.srcDirs("src/androidMain/res")
                        resources.srcDirs("src/commonMain/resources")
                    }
                }
            }
        }
    }
}

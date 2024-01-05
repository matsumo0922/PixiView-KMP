import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("pixiview.kmp")
    id("pixiview.kmp.android.application")
    id("pixiview.kmp.android.compose")
    id("pixiview.kmp.android")
    id("pixiview.kmp.ios")
    id("pixiview.detekt")
}

android {
    namespace = "me.matsumo.fanbox"

    val localProperties = Properties().apply {
        load(project.rootDir.resolve("local.properties").inputStream())
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("${project.rootDir}/gradle/keystore/debug.keystore")
        }
        create("release") {
            storeFile = file("${project.rootDir}/gradle/keystore/release.keystore")
            storePassword = localProperties.getProperty("storePassword") ?: System.getenv("RELEASE_STORE_PASSWORD")
            keyPassword = localProperties.getProperty("keyPassword") ?: System.getenv("RELEASE_KEY_PASSWORD")
            keyAlias = localProperties.getProperty("keyAlias") ?: System.getenv("RELEASE_KEY_ALIAS")
        }
        create("billing") {
            storeFile = file("${project.rootDir}/gradle/keystore/release.keystore")
            storePassword = localProperties.getProperty("storePassword") ?: System.getenv("RELEASE_STORE_PASSWORD")
            keyPassword = localProperties.getProperty("keyPassword") ?: System.getenv("RELEASE_KEY_PASSWORD")
            keyAlias = localProperties.getProperty("keyAlias") ?: System.getenv("RELEASE_KEY_ALIAS")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
            versionNameSuffix = ".D"
            applicationIdSuffix = ".debug"
        }
        create("billing") {
            signingConfig = signingConfigs.getByName("billing")
            isDebuggable = true
            matchingFallbacks.add("debug")
        }
    }

    dependencies {
        debugImplementation(libs.facebook.flipper)
        debugImplementation(libs.facebook.flipper.network)
        debugImplementation(libs.facebook.flipper.leakcanary)
        debugImplementation(libs.facebook.soloader)
        // debugImplementation(libs.leakcanary)
    }
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core:common"))
                implementation(project(":core:model"))
                implementation(project(":core:datastore"))
                implementation(project(":core:repository"))
                implementation(project(":core:ui"))
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.androidx.core.splashscreen)
                // implementation(libs.play.review)
                // implementation(libs.play.update)
                // implementation(libs.play.service.oss)
                // implementation(libs.play.service.ads)
                implementation(libs.google.material)
                implementation(libs.ktor.okhttp)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation(libs.ktor.darwin)
            }
        }
    }
}

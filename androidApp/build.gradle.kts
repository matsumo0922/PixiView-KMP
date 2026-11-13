@file:Suppress("UnusedPrivateProperty")

import com.android.build.api.variant.ResValue
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("pixiview.primitive.android.application")
    id("pixiview.primitive.detekt")
}

val localProperties = Properties().apply {
    project.rootDir.resolve("local.properties").also {
        if (it.exists()) load(it.inputStream())
    }
}

// This ID must be valid or the app will crash.
// When building from GitHub, either exclude AdMob code or register with AdMob for an ID.
val admobTestAppId = "ca-app-pub-0000000000000000~0000000000"

// リリースビルドで検査する Android 用の広告 ID。アプリが値を読む経路と同じ順で解決する
// アプリ ID はこのファイルのマニフェストに、広告ユニット ID は shared の BuildKonfig に埋め込まれる
val releaseAdMobIds = mapOf(
    "ADMOB_ANDROID_APP_ID" to localProperties.getProperty("ADMOB_ANDROID_APP_ID"),
    "ADMOB_ANDROID_BANNER_AD_UNIT_ID" to readLocalPropertyOrEnv("ADMOB_ANDROID_BANNER_AD_UNIT_ID"),
    "ADMOB_ANDROID_NATIVE_AD_UNIT_ID" to readLocalPropertyOrEnv("ADMOB_ANDROID_NATIVE_AD_UNIT_ID"),
    "ADMOB_ANDROID_REWARD_AD_UNIT_ID" to readLocalPropertyOrEnv("ADMOB_ANDROID_REWARD_AD_UNIT_ID"),
    "ADMOB_ANDROID_INTERSTITIAL_AD_UNIT_ID" to readLocalPropertyOrEnv("ADMOB_ANDROID_INTERSTITIAL_AD_UNIT_ID"),
)

val verifyReleaseAdMobIds by tasks.registering {
    val testIdKeys = releaseAdMobIds.filterValues(::isAdMobTestId).keys.toList()
    inputs.property("testIdKeys", testIdKeys)

    doLast {
        check(testIdKeys.isEmpty()) { "Release builds must not use AdMob test IDs. Set these keys in local.properties: ${testIdKeys.joinToString()}" }
    }
}

fun readLocalPropertyOrEnv(key: String): String? = localProperties.getProperty(key) ?: System.getenv(key)

// Google のテスト用広告ユニット ID は publisher ID が共通しているため、前方一致で判定する
fun isAdMobTestId(id: String?): Boolean = when {
    id.isNullOrBlank() -> true
    id == admobTestAppId -> true
    else -> id.startsWith("ca-app-pub-3940256099942544")
}

android {
    namespace = "me.matsumo.fanbox"

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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    androidComponents {
        onVariants {
            val appName = when (it.buildType) {
                "debug" -> "FANBOX Debug"
                "billing" -> "FANBOX Billing"
                else -> "FANBOX"
            }

            it.manifestPlaceholders.apply {
                put("ADMOB_ANDROID_APP_ID", localProperties.getProperty("ADMOB_ANDROID_APP_ID") ?: admobTestAppId)
                put("ADMOB_IOS_APP_ID", localProperties.getProperty("ADMOB_IOS_APP_ID") ?: admobTestAppId)
            }

            it.resValues.apply {
                put(it.makeResValueKey("string", "app_name"), ResValue(appName, null))
            }

            if (it.buildType == "release") {
                it.packaging.resources.excludes.add("META-INF/**")
                it.lifecycleTasks.registerPreBuild(verifyReleaseAdMobIds)
            }
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:common"))
    implementation(project(":core:logs"))
    implementation(project(":core:model"))
    implementation(project(":core:repository"))
    implementation(project(":core:ui"))
    implementation(project(":feature:service"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    implementation(libs.bundles.firebase)
    implementation(libs.bundles.mediation)
    implementation(libs.bundles.ui.android.api)

    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.coil3.gif)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.startup)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.napier)
    implementation(libs.play.review)
    implementation(libs.play.update)

    debugImplementation(libs.androidx.compose.ui.tooling)
}

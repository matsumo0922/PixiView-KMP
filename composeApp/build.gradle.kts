@file:Suppress("UnusedPrivateProperty")

import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.ResValue
import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.Serializable

plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.kmp.android.application")
    id("pixiview.primitive.kmp.android.compose")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

// This ID must be valid or the app will crash.
// When building from GitHub, either exclude AdMob code or register with AdMob for an ID.
val admobTestAppId = "ca-app-pub-0000000000000000~0000000000"
val bannerAdTestId = "ca-app-pub-3940256099942544/6300978111"
val nativeAdTestId = "ca-app-pub-3940256099942544/2247696110"
val rewardAdTestId = "ca-app-pub-3940256099942544/5224354917"

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

    androidComponents {
        onVariants {
            val appName = when (it.buildType) {
                "debug" -> "FANBOX Debug"
                "billing" -> "FANBOX Billing"
                else -> "FANBOX"
            }

            it.manifestPlaceholders.apply {
                putManifestPlaceholder(localProperties, "ADMOB_ANDROID_APP_ID", defaultValue = admobTestAppId)
                putManifestPlaceholder(localProperties, "ADMOB_IOS_APP_ID", defaultValue = admobTestAppId)
            }

            it.buildConfigFields.apply {
                putBuildConfig(localProperties, "ADMOB_ANDROID_APP_ID", defaultValue = admobTestAppId)
                putBuildConfig(localProperties, "ADMOB_ANDROID_BANNER_AD_UNIT_ID", if (it.buildType != "release") bannerAdTestId else null)
                putBuildConfig(localProperties, "ADMOB_ANDROID_NATIVE_AD_UNIT_ID", if (it.buildType != "release") nativeAdTestId else null)
                putBuildConfig(localProperties, "ADMOB_ANDROID_REWARD_AD_UNIT_ID", if (it.buildType != "release") rewardAdTestId else null)
            }

            it.resValues.apply {
                put(it.makeResValueKey("string", "app_name"), ResValue(appName, null))
            }

            if (it.buildType == "release") {
                it.packaging.resources.excludes.add("META-INF/**")
            }
        }
    }

    // https://youtrack.jetbrains.com/issue/CMP-6707/Compose-Multiplatform-Resources-and-Kotlin-2.1.0-Beta1-SyncComposeResourcesForIosTask-configuration-failure
    tasks.findByName("checkSandboxAndWriteProtection")?.dependsOn("syncComposeResourcesForIos")

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
                implementation(project(":core:billing"))
                implementation(project(":core:ui"))
                implementation(project(":core:logs"))
                implementation(project(":core:resources"))

                implementation(project(":feature:welcome"))
                implementation(project(":feature:library"))
                implementation(project(":feature:setting"))
                implementation(project(":feature:about"))
                implementation(project(":feature:post"))
                implementation(project(":feature:creator"))
                implementation(project(":feature:service"))

                implementation(libs.moko.permissions)
                implementation(libs.moko.permissions.compose)
                implementation(libs.moko.biometry)
                implementation(libs.moko.biometry.compose)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.splashscreen)
                implementation(libs.play.review)
                implementation(libs.play.update)
                implementation(libs.google.material)
                implementation(libs.koin.androidx.startup)
            }
        }
    }
}

buildkonfig {
    val localProperties = Properties().apply {
        load(project.rootDir.resolve("local.properties").inputStream())
    }

    packageName = "me.matsumo.fanbox"

    defaultConfigs {
        putBuildConfig(localProperties, "VERSION_NAME", libs.versions.versionName.get())
        putBuildConfig(localProperties, "VERSION_CODE", libs.versions.versionCode.get())
        putBuildConfig(localProperties, "DEVELOPER_PASSWORD")
        putBuildConfig(localProperties, "PIXIV_CLIENT_ID")
        putBuildConfig(localProperties, "PIXIV_CLIENT_SECRET")
        putBuildConfig(localProperties, "ADMOB_ANDROID_APP_ID", defaultValue = admobTestAppId)
        putBuildConfig(localProperties, "ADMOB_ANDROID_BANNER_AD_UNIT_ID", defaultValue = bannerAdTestId)
        putBuildConfig(localProperties, "ADMOB_ANDROID_NATIVE_AD_UNIT_ID", defaultValue = nativeAdTestId)
        putBuildConfig(localProperties, "ADMOB_ANDROID_REWARD_AD_UNIT_ID", defaultValue = rewardAdTestId)
        putBuildConfig(localProperties, "ADMOB_IOS_APP_ID", defaultValue = admobTestAppId)
        putBuildConfig(localProperties, "ADMOB_IOS_BANNER_AD_UNIT_ID", defaultValue = bannerAdTestId)
        putBuildConfig(localProperties, "ADMOB_IOS_NATIVE_AD_UNIT_ID", defaultValue = nativeAdTestId)
        putBuildConfig(localProperties, "ADMOB_IOS_REWARD_AD_UNIT_ID", defaultValue = rewardAdTestId)
    }
}

fun TargetConfigDsl.putBuildConfig(
    localProperties: Properties,
    key: String,
    value: String? = null,
    defaultValue: String = "",
) {
    val property = localProperties.getProperty(key)
    val env = System.getenv(key)

    buildConfigField(FieldSpec.Type.STRING, key, (value ?: property ?: env ?: defaultValue).replace("\"", ""))
}

fun MapProperty<String, BuildConfigField<out Serializable>>.putBuildConfig(
    localProperties: Properties,
    key: String,
    value: String? = null,
    type: String = "String",
    defaultValue: String = "",
    comment: String? = null,
) {
    val property = localProperties.getProperty(key)
    val env = System.getenv(key)

    put(key, BuildConfigField(type, (value ?: property ?: env ?: defaultValue).toStringLiteral(), comment))
}

fun MapProperty<String, String>.putManifestPlaceholder(
    localProperties: Properties,
    key: String,
    value: String? = null,
    defaultValue: String = "",
) {
    val property = localProperties.getProperty(key)
    val env = System.getenv(key)

    put(key, (value ?: property ?: env ?: defaultValue).replace("\"", ""))
}

fun Any.toStringLiteral(): String {
    val value = toString()

    if (value.firstOrNull() == '\"' && value.lastOrNull() == '\"') {
        return value
    }

    return "\"$value\""
}

@file:Suppress("UnusedPrivateProperty")

import com.android.build.api.variant.ResValue
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.konan.properties.Properties

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
                put("ADMOB_ANDROID_APP_ID", localProperties.getProperty("ADMOB_ANDROID_APP_ID") ?: admobTestAppId)
                put("ADMOB_IOS_APP_ID", localProperties.getProperty("ADMOB_IOS_APP_ID") ?: admobTestAppId)
            }

            it.resValues.apply {
                put(it.makeResValueKey("string", "app_name"), ResValue(appName, null))
            }

            if (it.buildType == "release") {
                it.packaging.resources.excludes.add("META-INF/**")
            }
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
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

        androidMain.dependencies {
            implementation(libs.bundles.mediation)

            implementation(libs.androidx.core.splashscreen)
            implementation(libs.play.review)
            implementation(libs.play.update)
            implementation(libs.google.material)
            implementation(libs.koin.androidx.startup)
        }
    }
}

buildkonfig {
    val localProperties = Properties().apply {
        load(project.rootDir.resolve("local.properties").inputStream())
    }

    packageName = "me.matsumo.fanbox"

    defaultConfigs {
        fun putBuildConfig(
            key: String,
            value: String? = null,
            defaultValue: String = "",
        ) {
            val property = localProperties.getProperty(key)
            val env = System.getenv(key)

            buildConfigField(FieldSpec.Type.STRING, key, (value ?: property ?: env ?: defaultValue).replace("\"", ""))
        }

        putBuildConfig("VERSION_NAME", libs.versions.versionName.get())
        putBuildConfig("VERSION_CODE", libs.versions.versionCode.get())
        putBuildConfig("DEVELOPER_PASSWORD")
        putBuildConfig("PIXIV_CLIENT_ID")
        putBuildConfig("PIXIV_CLIENT_SECRET")

        putBuildConfig("ADMOB_ANDROID_APP_ID", defaultValue = admobTestAppId)
        putBuildConfig("ADMOB_ANDROID_BANNER_AD_UNIT_ID", defaultValue = bannerAdTestId)
        putBuildConfig("ADMOB_ANDROID_NATIVE_AD_UNIT_ID", defaultValue = nativeAdTestId)
        putBuildConfig("ADMOB_ANDROID_REWARD_AD_UNIT_ID", defaultValue = rewardAdTestId)

        putBuildConfig("ADMOB_IOS_APP_ID", defaultValue = admobTestAppId)
        putBuildConfig("ADMOB_IOS_BANNER_AD_UNIT_ID", defaultValue = bannerAdTestId)
        putBuildConfig("ADMOB_IOS_NATIVE_AD_UNIT_ID", defaultValue = nativeAdTestId)
        putBuildConfig("ADMOB_IOS_REWARD_AD_UNIT_ID", defaultValue = rewardAdTestId)

        putBuildConfig("OPENAI_API_KEY")
    }
}

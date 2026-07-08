@file:Suppress("UnusedPrivateProperty")

import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.android.library")
    id("pixiview.primitive.kmp.compose")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
    alias(libs.plugins.build.konfig)
}

val localProperties = Properties().apply {
    project.rootDir.resolve("local.properties").also {
        if (it.exists()) load(it.inputStream())
    }
}

// This ID must be valid or the app will crash.
// When building from GitHub, either exclude AdMob code or register with AdMob for an ID.
val admobTestAppId = "ca-app-pub-0000000000000000~0000000000"
val bannerAdTestId = "ca-app-pub-3940256099942544/6300978111"
val nativeAdTestId = "ca-app-pub-3940256099942544/2247696110"
val rewardAdTestId = "ca-app-pub-3940256099942544/5224354917"
val interstitialAdTestId = "ca-app-pub-3940256099942544/1033173712"
val appOpenAdTestId = "ca-app-pub-3940256099942544/9257395921"

kotlin {
    android {
        namespace = "me.matsumo.fanbox.shared"
    }

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
    }
}

buildkonfig {
    packageName = "me.matsumo.fanbox"
    exposeObjectWithName = "BuildKonfig"

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
        putBuildConfig("ADMOB_ANDROID_INTERSTITIAL_AD_UNIT_ID", defaultValue = interstitialAdTestId)
        putBuildConfig("ADMOB_ANDROID_APP_OPEN_AD_UNIT_ID", defaultValue = appOpenAdTestId)

        putBuildConfig("ADMOB_IOS_APP_ID", defaultValue = admobTestAppId)
        putBuildConfig("ADMOB_IOS_BANNER_AD_UNIT_ID", defaultValue = bannerAdTestId)
        putBuildConfig("ADMOB_IOS_NATIVE_AD_UNIT_ID", defaultValue = nativeAdTestId)
        putBuildConfig("ADMOB_IOS_REWARD_AD_UNIT_ID", defaultValue = rewardAdTestId)
        putBuildConfig("ADMOB_IOS_INTERSTITIAL_AD_UNIT_ID", defaultValue = interstitialAdTestId)
        putBuildConfig("ADMOB_IOS_APP_OPEN_AD_UNIT_ID", defaultValue = appOpenAdTestId)

        putBuildConfig("OPENAI_API_KEY")

        putBuildConfig("PURCHASE_ANDROID_API_KEY")
        putBuildConfig("PURCHASE_IOS_API_KEY")

        putBuildConfig("APPLOVIN_SDK_KEY")
    }
}

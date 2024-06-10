package me.matsumo.fanbox.di

import android.os.Build
import me.matsumo.fanbox.BuildConfig
import me.matsumo.fanbox.BuildKonfig
import me.matsumo.fanbox.core.common.PixiViewConfig

actual fun getPixiViewConfig(): PixiViewConfig {
    val release = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Build.VERSION.RELEASE_OR_CODENAME else Build.VERSION.RELEASE

    return PixiViewConfig(
        versionCode = BuildKonfig.VERSION_CODE,
        versionName = BuildKonfig.VERSION_NAME,
        developerPassword = BuildKonfig.DEVELOPER_PASSWORD,
        pixivClientId = BuildKonfig.PIXIV_CLIENT_ID,
        pixivClientSecret = BuildKonfig.PIXIV_CLIENT_SECRET,
        adMobAndroid = PixiViewConfig.AdMob(
            appId = BuildKonfig.ADMOB_ANDROID_APP_ID,
            bannerAdUnitId = BuildConfig.ADMOB_ANDROID_BANNER_AD_UNIT_ID,
            nativeAdUnitId = BuildConfig.ADMOB_ANDROID_NATIVE_AD_UNIT_ID,
            rewardAdUnitId = BuildConfig.ADMOB_ANDROID_REWARD_AD_UNIT_ID,
        ),
        adMobIos = PixiViewConfig.AdMob(
            appId = BuildKonfig.ADMOB_IOS_APP_ID,
            bannerAdUnitId = BuildKonfig.ADMOB_IOS_BANNER_AD_UNIT_ID,
            nativeAdUnitId = BuildKonfig.ADMOB_IOS_NATIVE_AD_UNIT_ID,
            rewardAdUnitId = BuildKonfig.ADMOB_IOS_REWARD_AD_UNIT_ID,
        ),
        platform = "Android",
        platformVersion = "${release}(${Build.VERSION.SDK_INT})",
        device = "${Build.MODEL}(${Build.MANUFACTURER})",
        deviceAbis = Build.SUPPORTED_ABIS.contentToString(),
    )
}

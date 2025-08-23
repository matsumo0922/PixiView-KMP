package me.matsumo.fanbox.di

import android.os.Build
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
        bannerAdUnitId = BuildKonfig.ADMOB_ANDROID_BANNER_AD_UNIT_ID,
        nativeAdUnitId = BuildKonfig.ADMOB_ANDROID_NATIVE_AD_UNIT_ID,
        rewardAdUnitId = BuildKonfig.ADMOB_ANDROID_REWARD_AD_UNIT_ID,
        interstitialAdUnitId = BuildKonfig.ADMOB_ANDROID_INTERSTITIAL_AD_UNIT_ID,
        appOpenAdUnitId = BuildKonfig.ADMOB_ANDROID_APP_OPEN_AD_UNIT_ID,
        platform = "Android",
        platformVersion = "$release(${Build.VERSION.SDK_INT})",
        device = "${Build.MODEL}(${Build.MANUFACTURER})",
        deviceAbis = Build.SUPPORTED_ABIS.contentToString(),
        openaiApiKey = BuildKonfig.OPENAI_API_KEY,
        purchaseApiKey = BuildKonfig.PURCHASE_ANDROID_API_KEY,
    )
}

package me.matsumo.fanbox.di

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import me.matsumo.fanbox.BuildKonfig
import me.matsumo.fanbox.core.common.PixiViewConfig
import platform.UIKit.UIDevice
import platform.posix.uname
import platform.posix.utsname

@OptIn(ExperimentalForeignApi::class)
actual fun getPixiViewConfig(): PixiViewConfig {
    val device = UIDevice.currentDevice
    val architectureName = memScoped {
        val systemInfo = alloc<utsname>()
        uname(systemInfo.ptr)
        systemInfo.machine.toKString()
    }

    return PixiViewConfig(
        versionCode = BuildKonfig.VERSION_CODE,
        versionName = BuildKonfig.VERSION_NAME,
        developerPassword = BuildKonfig.DEVELOPER_PASSWORD,
        pixivClientId = BuildKonfig.PIXIV_CLIENT_ID,
        pixivClientSecret = BuildKonfig.PIXIV_CLIENT_SECRET,
        adMobAndroid = PixiViewConfig.AdMob(
            appId = BuildKonfig.ADMOB_ANDROID_APP_ID,
            bannerAdUnitId = BuildKonfig.ADMOB_ANDROID_BANNER_AD_UNIT_ID,
            nativeAdUnitId = BuildKonfig.ADMOB_ANDROID_NATIVE_AD_UNIT_ID,
            rewardAdUnitId = BuildKonfig.ADMOB_ANDROID_REWARD_AD_UNIT_ID,
        ),
        adMobIos = PixiViewConfig.AdMob(
            appId = BuildKonfig.ADMOB_IOS_APP_ID,
            bannerAdUnitId = BuildKonfig.ADMOB_IOS_BANNER_AD_UNIT_ID,
            nativeAdUnitId = BuildKonfig.ADMOB_IOS_NATIVE_AD_UNIT_ID,
            rewardAdUnitId = BuildKonfig.ADMOB_IOS_REWARD_AD_UNIT_ID,
        ),
        platform = "iOS",
        platformVersion = "${device.systemVersion}(${device.systemName})",
        device = "${device.model}(${device.name})",
        deviceAbis = architectureName,
        openaiApiKey = BuildKonfig.OPENAI_API_KEY,
    )
}

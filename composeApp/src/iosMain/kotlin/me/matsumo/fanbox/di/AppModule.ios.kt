package me.matsumo.fanbox.di

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import me.matsumo.fanbox.BuildKonfig
import me.matsumo.fanbox.core.common.PixiViewConfig
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.UIKit.UIDevice
import platform.posix.uname
import platform.posix.utsname

actual val appPlatformModule: Module = module {
    single {
        val device = UIDevice.currentDevice
        val architectureName = memScoped {
            val systemInfo = alloc<utsname>()
            uname(systemInfo.ptr)
            systemInfo.machine.toKString()
        }
        PixiViewConfig(
            versionCode = BuildKonfig.VERSION_CODE,
            versionName = BuildKonfig.VERSION_NAME,
            developerPassword = BuildKonfig.DEVELOPER_PASSWORD,
            pixivClientId = BuildKonfig.PIXIV_CLIENT_ID,
            pixivClientSecret = BuildKonfig.PIXIV_CLIENT_SECRET,
            bannerAdUnitId = BuildKonfig.ADMOB_IOS_BANNER_AD_UNIT_ID,
            nativeAdUnitId = BuildKonfig.ADMOB_IOS_NATIVE_AD_UNIT_ID,
            rewardAdUnitId = BuildKonfig.ADMOB_IOS_REWARD_AD_UNIT_ID,
            interstitialAdUnitId = BuildKonfig.ADMOB_IOS_INTERSTITIAL_AD_UNIT_ID,
            appOpenAdUnitId = BuildKonfig.ADMOB_IOS_APP_OPEN_AD_UNIT_ID,
            platform = "iOS",
            platformVersion = "${device.systemVersion}(${device.systemName})",
            device = "${device.model}(${device.name})",
            deviceAbis = architectureName,
            openaiApiKey = BuildKonfig.OPENAI_API_KEY,
            purchaseApiKey = BuildKonfig.PURCHASE_IOS_API_KEY,
        )
    }
}

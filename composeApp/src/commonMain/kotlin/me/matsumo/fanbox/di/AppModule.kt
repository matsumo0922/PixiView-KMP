package me.matsumo.fanbox.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.matsumo.fanbox.BuildKonfig
import me.matsumo.fanbox.PixiViewViewModel
import me.matsumo.fanbox.core.common.PixiViewConfig
import org.koin.dsl.module

val appModule = module {

    single {
        PixiViewConfig(
            versionCode = BuildKonfig.VERSION_CODE,
            versionName = BuildKonfig.VERSION_NAME,
            developerPassword = BuildKonfig.DEVELOPER_PASSWORD,
            pixivClientId = BuildKonfig.PIXIV_CLIENT_ID,
            pixivClientSecret = BuildKonfig.PIXIV_CLIENT_SECRET,
            adMobAndroid = PixiViewConfig.AdMob(
                appId = BuildKonfig.ADMOB_ANDROID_APP_ID,
                bannerAdUnitId = BuildKonfig.ADMOB_ANDROID_BANNER_AD_UNIT_ID,
                nativeAdUnitId = BuildKonfig.ADMOB_ANDROID_NATIVE_AD_UNIT_ID,
            ),
            adMobIos = PixiViewConfig.AdMob(
                appId = BuildKonfig.ADMOB_IOS_APP_ID,
                bannerAdUnitId = BuildKonfig.ADMOB_IOS_BANNER_AD_UNIT_ID,
                nativeAdUnitId = BuildKonfig.ADMOB_IOS_NATIVE_AD_UNIT_ID,
            ),
        )
    }

    single<CoroutineDispatcher> {
        Dispatchers.IO
    }

    factory { 
        PixiViewViewModel(
            userDataRepository = get(),
            fanboxRepository = get(),
            billingStatus = get(),
        )
    }
}

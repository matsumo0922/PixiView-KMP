package me.matsumo.fanbox.di

import me.matsumo.fanbox.BuildKonfig
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
            adMobAppId = BuildKonfig.ADMOB_APP_ID,
            adMobBannerAdUnitId = BuildKonfig.ADMOB_BANNER_AD_UNIT_ID,
            adMobNativeAdUnitId = BuildKonfig.ADMOB_NATIVE_AD_UNIT_ID,
        )
    }
}

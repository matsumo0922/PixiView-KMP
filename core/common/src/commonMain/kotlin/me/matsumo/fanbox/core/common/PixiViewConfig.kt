package me.matsumo.fanbox.core.common

data class PixiViewConfig(
    val versionCode: String,
    val versionName: String,
    val developerPassword: String,
    val pixivClientId: String,
    val pixivClientSecret: String,
    val adMobAppId: String,
    val adMobBannerAdUnitId: String,
    val adMobNativeAdUnitId: String,
) {
    companion object {
        fun dummy(): PixiViewConfig {
            return PixiViewConfig(
                versionCode = "223",
                versionName = "1.4.21",
                developerPassword = "1919191919",
                pixivClientId = "1919191919",
                pixivClientSecret = "1919191919",
                adMobAppId = "ca-app-pub-1919191919~1919191919",
                adMobBannerAdUnitId = "ca-app-pub-1919191919/1919191919",
                adMobNativeAdUnitId = "ca-app-pub-1919191919/1919191919",
            )
        }
    }
}

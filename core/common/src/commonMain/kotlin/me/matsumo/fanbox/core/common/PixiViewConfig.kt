package me.matsumo.fanbox.core.common

data class PixiViewConfig(
    val versionCode: String,
    val versionName: String,
    val developerPassword: String,
    val pixivClientId: String,
    val pixivClientSecret: String,
    val bannerAdUnitId: String,
    val nativeAdUnitId: String,
    val rewardAdUnitId: String,
    val interstitialAdUnitId: String,
    val appOpenAdUnitId: String,
    val platform: String,
    val platformVersion: String,
    val device: String,
    val deviceAbis: String,
    val openaiApiKey: String,
    val purchaseApiKey: String?,
) {
    companion object {
        fun dummy(): PixiViewConfig {
            return PixiViewConfig(
                versionCode = "223",
                versionName = "1.4.21",
                developerPassword = "1919191919",
                pixivClientId = "1919191919",
                pixivClientSecret = "1919191919",
                bannerAdUnitId = "ca-app-pub-1919191919~1919191919",
                nativeAdUnitId = "ca-app-pub-1919191919~1919191919",
                rewardAdUnitId = "ca-app-pub-1919191919~1919191919",
                interstitialAdUnitId = "ca-app-pub-1919191919~1919191919",
                appOpenAdUnitId = "ca-app-pub-1919191919~1919191919",
                platform = "android",
                platformVersion = "12(32)",
                device = "Pixel 6(Google)",
                deviceAbis = "arm64-v8a,armeabi-v7a",
                openaiApiKey = "1919191919",
                purchaseApiKey = null,
            )
        }
    }
}

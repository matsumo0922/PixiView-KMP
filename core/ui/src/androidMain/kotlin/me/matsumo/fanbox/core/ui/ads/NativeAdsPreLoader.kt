package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.PixiViewConfig

class NativeAdsPreLoader(
    context: Context,
    pixiViewConfig: PixiViewConfig,
    ioDispatcher: CoroutineDispatcher,
) {
    private val scope = CoroutineScope(ioDispatcher)
    private val preloadedNativeAds: MutableList<NativeAd> = mutableListOf()
    private val keyMap: MutableMap<String, NativeAd> = mutableMapOf()
    private val adLoader: AdLoader

    init {
        val nativeAdOptions = NativeAdOptions.Builder()
            .setVideoOptions(
                VideoOptions.Builder()
                    .setStartMuted(true)
                    .setClickToExpandRequested(true)
                    .build(),
            )
            .setMediaAspectRatio(MediaAspectRatio.LANDSCAPE)
            .setRequestMultipleImages(false)
            .setReturnUrlsForImageAssets(false)
            .build()

        val adListener = object : AdListener() {
            override fun onAdFailedToLoad(cause: LoadAdError) {
                Napier.e("onAdFailedToLoad: ${cause.message}")
            }
        }

        adLoader = AdLoader.Builder(context, pixiViewConfig.nativeAdUnitId)
            .withNativeAdOptions(nativeAdOptions)
            .withAdListener(adListener)
            .forNativeAd { nativeAd ->
                preloadedNativeAds.add(nativeAd)
            }
            .build()

        preloadAd()
    }

    @SuppressLint("MissingPermission")
    fun preloadAd() {
        if (adLoader.isLoading) return

        scope.launch {
            val numberOfAds = NUMBER_OF_PRELOAD_ADS - preloadedNativeAds.count()
            if (numberOfAds > 0) {
                adLoader.loadAds(AdRequest.Builder().build(), numberOfAds)
            }
        }
    }

    fun getNativeAd(key: String): NativeAd? {
        Napier.d("getNativeAd: $key, ${keyMap.containsKey(key)}, ${keyMap.size}, ${preloadedNativeAds.size}")

        keyMap[key]?.let { return it }
        preloadedNativeAds.removeFirstOrNull()?.also {
            preloadAd()

            keyMap[key] = it
            return it
        }

        return null
    }

    fun popAd(key: String) {
        Napier.d("popAd: $key")
        keyMap.remove(key)?.destroy()
    }

    companion object {
        const val NUMBER_OF_PRELOAD_ADS = 4
    }
}

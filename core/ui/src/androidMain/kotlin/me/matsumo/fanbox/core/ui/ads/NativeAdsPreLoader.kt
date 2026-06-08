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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.matsumo.fanbox.core.common.PixiViewConfig

/** Android ネイティブ広告のプリロード在庫と表示キーへの割り当てを管理するクラス。 */
class NativeAdsPreLoader(
    context: Context,
    pixiViewConfig: PixiViewConfig,
) {
    private val preloadedNativeAds: MutableList<NativeAd> = mutableListOf()
    private val keyMap: MutableMap<String, NativeAd> = mutableMapOf()
    private val retryController = AdLoadRetryController(adFormatName = "NativeAds")
    private val _nativeAdInventoryVersion = MutableStateFlow(0)
    private val adLoader: AdLoader
    val nativeAdInventoryVersion: StateFlow<Int> = _nativeAdInventoryVersion.asStateFlow()

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
                retryController.scheduleRetry(
                    failureMessage = cause.toString(),
                    retryAction = ::retryPreloadAd,
                )
            }
        }

        adLoader = AdLoader.Builder(context, pixiViewConfig.nativeAdUnitId)
            .withNativeAdOptions(nativeAdOptions)
            .withAdListener(adListener)
            .forNativeAd(::onNativeAdLoaded)
            .build()
    }

    private fun preloadAd() {
        preloadAd(isRetry = false)
    }

    @SuppressLint("MissingPermission")
    private fun preloadAd(isRetry: Boolean) {
        if (!isRetry) {
            retryController.reset()
        }

        if (adLoader.isLoading) return

        loadMissingNativeAds()
    }

    private fun retryPreloadAd() {
        preloadAd(isRetry = true)
    }

    fun getNativeAd(key: String): NativeAd? {
        Napier.d("getNativeAd: $key, ${keyMap.containsKey(key)}, ${keyMap.size}, ${preloadedNativeAds.size}")

        val mappedNativeAd = keyMap[key]
        if (mappedNativeAd != null) {
            return mappedNativeAd
        }

        val preloadedNativeAd = preloadedNativeAds.removeFirstOrNull()
        if (preloadedNativeAd == null) {
            preloadAd()
            return null
        }

        preloadAd()
        keyMap[key] = preloadedNativeAd

        return preloadedNativeAd
    }

    fun popAd(key: String) {
        Napier.d("popAd: $key")
        keyMap.remove(key)?.destroy()
    }

    private fun loadMissingNativeAds() {
        val numberOfAds = NUMBER_OF_PRELOAD_ADS - preloadedNativeAds.count()
        if (numberOfAds > 0) {
            adLoader.loadAds(AdRequest.Builder().build(), numberOfAds)
        }
    }

    private fun onNativeAdLoaded(nativeAd: NativeAd) {
        retryController.reset()
        preloadedNativeAds.add(nativeAd)
        _nativeAdInventoryVersion.update { currentVersion -> currentVersion + 1 }
    }
}

/** プリロードして保持するネイティブ広告の最大数。 */
private const val NUMBER_OF_PRELOAD_ADS = 4

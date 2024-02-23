package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Stable
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.PixiViewConfig
import java.time.LocalDateTime

@Stable
data class PreLoadedNativeAd(
    val key: Int = 0,
    val ad: NativeAd,
    val date: LocalDateTime,
)

class NativeAdsPreLoader(
    private val context: Context,
    private val pixiViewConfig: PixiViewConfig,
    private val iosDispatcher: CoroutineDispatcher,
) {

    private val scope = CoroutineScope(iosDispatcher)
    private var preloadedNativeAds: MutableList<PreLoadedNativeAd> = mutableListOf()
    private var adLoader: AdLoader
    private var key = 0

    init {
        adLoader = AdLoader.Builder(context, pixiViewConfig.adMobAndroid.nativeAdUnitId)
            .forNativeAd { nativeAd ->
                preloadedNativeAds.add(
                    PreLoadedNativeAd(
                        key = key,
                        ad = nativeAd,
                        date = LocalDateTime.now(),
                    ),
                )

                key++
            }
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        preloadAd()
    }

    @SuppressLint("MissingPermission")
    fun preloadAd() {
        if (adLoader.isLoading) return

        removeExpiredAds()

        val numberOfAds = NUMBER_OF_PRELOAD_ADS - preloadedNativeAds.count()

        if (numberOfAds > 0) {
            adLoader.loadAds(AdRequest.Builder().build(), numberOfAds)
        }
    }

    fun getKey(): Int? {
        removeExpiredAds()

        val first = preloadedNativeAds.firstOrNull()

        return if (first != null) {
            // ここでプリロードしてしまうと、フリークエンシーキャップを設定している場合の効果がなくなる.
            // 従って、広告を表示する少し前にプリロードするのが望ましい
            // preloadAd()

            if (preloadedNativeAds.size < 1) {
                scope.launch { preloadAd() }
            }

            first.key
        } else {
            scope.launch { preloadAd() }
            null
        }
    }

    fun getAd(key: Int): NativeAd? {
        return preloadedNativeAds.firstOrNull { it.key == key }?.ad
    }

    fun popAd(key: Int) {
        preloadedNativeAds.removeIf { it.key == key }
    }

    /** 期限切れのNativeAdを削除する */
    private fun removeExpiredAds() {
        val adLimitDate = LocalDateTime.now().minusHours(1)
        preloadedNativeAds = preloadedNativeAds.filter { it.date > adLimitDate }.toMutableList()
    }

    companion object {
        const val NUMBER_OF_PRELOAD_ADS = 4
    }
}

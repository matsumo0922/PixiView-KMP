package me.matsumo.fanbox.core.ui.ads

import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.matsumo.fanbox.core.common.PixiViewConfig

class RewardAdLoader(
    private val context: Context,
    private val pixiViewConfig: PixiViewConfig,
) {
    private val _rewardAd = MutableStateFlow<RewardedAd?>(null)
    val rewardAd = _rewardAd.asStateFlow()

    private var callback: FullScreenContentCallback? = null
    private var isLoading = false

    fun loadRewardAdIfNeeded() {
        if (_rewardAd.value != null || isLoading) return

        isLoading = true

        callback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Napier.d { "onAdFailedToShowFullScreenContent: ${error.message}" }
                _rewardAd.value = null
            }

            override fun onAdShowedFullScreenContent() {
                Napier.d { "onAdShowedFullScreenContent" }
                _rewardAd.value = null
            }
        }

        val adRequest = AdRequest.Builder().build()
        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Napier.d { "onAdLoaded" }
                _rewardAd.value = rewardedAd
                _rewardAd.value!!.fullScreenContentCallback = callback
                isLoading = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Napier.e { "onAdFailedToLoad: ${error.message}" }
                isLoading = false
            }
        }

        RewardedAd.load(context, pixiViewConfig.rewardAdUnitId, adRequest, adLoadCallback)
    }
}

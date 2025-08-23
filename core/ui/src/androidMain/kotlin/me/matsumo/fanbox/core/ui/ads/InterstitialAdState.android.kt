package me.matsumo.fanbox.core.ui.ads

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.resume

@Stable
class InterstitialAdStateImpl internal constructor(
    private val activity: Activity,
    private val adUnitId: String,
    private val enable: Boolean,
) : InterstitialAdState {
    private var interstitialAd: InterstitialAd? = null
    private var loaded = false
    private var loadRequestId = 0

    override fun load() {
        if (!enable || loaded) return

        val requestId = ++loadRequestId
        val adRequest = AdRequest.Builder().build()

        val callback = object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                if (requestId != loadRequestId) return

                interstitialAd = ad
                loaded = true

                Napier.d("InterstitialAd: loaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                if (requestId != loadRequestId) return

                interstitialAd = null
                loaded = false

                Napier.w("InterstitialAd: failed to load, $loadAdError")
            }
        }

        InterstitialAd.load(activity, adUnitId, adRequest, callback)
    }

    @OptIn(ExperimentalAtomicApi::class)
    override suspend fun show(): Boolean {
        if (!enable || !loaded || interstitialAd == null) {
            Napier.w("InterstitialAd: not loaded, $enable, $loaded, $interstitialAd")
            return false
        }

        return suspendCancellableCoroutine { cont ->
            val handled = AtomicBoolean(false)

            interstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    if (handled.compareAndSet(false, true)) {
                        cleanup()
                        cont.resume(false)
                    }
                }

                override fun onAdDismissedFullScreenContent() {
                    if (handled.compareAndSet(false, true)) {
                        cleanup()
                        cont.resume(true)
                    }
                }
            }

            interstitialAd!!.show(activity)

            cont.invokeOnCancellation {
                if (handled.compareAndSet(false, true)) {
                    cleanup()
                }
            }
        }
    }

    private fun cleanup() {
        interstitialAd?.fullScreenContentCallback = null
        interstitialAd = null
        loaded = false

        load()
    }
}

@Composable
actual fun rememberInterstitialAdState(
    adUnitId: String,
    enable: Boolean,
): InterstitialAdState {
    val context = LocalActivity.current!!

    return remember(adUnitId, enable) {
        InterstitialAdStateImpl(context, adUnitId, enable)
    }
}

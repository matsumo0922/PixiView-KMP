package me.matsumo.fanbox.core.ui.ads

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

/** Android のインタースティシャル広告ロードと表示状態を管理するクラス。 */
@Stable
class InterstitialAdStateImpl internal constructor(
    private val activity: Activity,
    private val adUnitId: String,
    private val enable: Boolean,
) : InterstitialAdState {
    private var interstitialAd: InterstitialAd? = null
    private val retryController = AdLoadRetryController(adFormatName = "InterstitialAd")
    private var loaded = false
    private var loading = false
    private var loadRequestId = 0

    override fun load() {
        load(isRetry = false)
    }

    private fun load(isRetry: Boolean) {
        if (!enable || loaded || loading) return

        if (!isRetry) {
            retryController.reset()
        }

        loading = true
        val requestId = ++loadRequestId
        val adRequest = AdRequest.Builder().build()

        val callback = object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                if (requestId != loadRequestId) return

                interstitialAd = ad
                loaded = true
                loading = false
                retryController.reset()

                Napier.d("InterstitialAd: loaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                if (requestId != loadRequestId) return

                interstitialAd = null
                loaded = false
                loading = false

                retryController.scheduleRetry(
                    failureMessage = loadAdError.toString(),
                    retryAction = ::retryLoad,
                )
            }
        }

        InterstitialAd.load(activity, adUnitId, adRequest, callback)
    }

    private fun retryLoad() {
        load(isRetry = true)
    }

    @OptIn(ExperimentalAtomicApi::class)
    override suspend fun show(): Boolean {
        if (!enable || !loaded || interstitialAd == null) {
            Napier.w("InterstitialAd: not loaded, $enable, $loaded, $interstitialAd")
            load()
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

    internal fun dispose() {
        retryController.reset()
        interstitialAd?.fullScreenContentCallback = null
        interstitialAd = null
        loaded = false
        loading = false
        loadRequestId += 1
    }
}

@Composable
actual fun rememberInterstitialAdState(
    adUnitId: String,
    enable: Boolean,
): InterstitialAdState {
    val context = LocalActivity.current!!
    val interstitialAdState = remember(adUnitId, enable) {
        InterstitialAdStateImpl(context, adUnitId, enable)
    }

    DisposableEffect(interstitialAdState) {
        onDispose {
            interstitialAdState.dispose()
        }
    }

    return interstitialAdState
}

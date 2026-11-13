package me.matsumo.fanbox.core.ui.ads

import android.app.Activity
import android.os.SystemClock
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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
    private var hasLoadFailed = false
    private var loadedAtElapsedRealtime = 0L
    private var loadRequestId = 0

    override fun load() {
        load(isRetry = false)
    }

    private fun load(isRetry: Boolean) {
        val shouldSkipLoad = !enable || loaded || loading
        if (shouldSkipLoad) return

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
                hasLoadFailed = false
                loadedAtElapsedRealtime = SystemClock.elapsedRealtime()
                retryController.reset()

                Napier.d("InterstitialAd: loaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                if (requestId != loadRequestId) return

                interstitialAd = null
                loaded = false
                loading = false
                hasLoadFailed = true

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

    override suspend fun show(): InterstitialAdShowResult {
        if (!enable) return InterstitialAdShowResult.Disabled

        val loadedAd = interstitialAd
        if (!loaded || loadedAd == null) {
            // load() で状態が変わる前に、表示できなかった理由を決める
            val notLoadedResult = if (!loading && hasLoadFailed) InterstitialAdShowResult.LoadFailed else InterstitialAdShowResult.Loading

            Napier.w { "InterstitialAd: not loaded. result=$notLoadedResult" }
            load()

            return notLoadedResult
        }

        if (isLoadedAdExpired()) {
            Napier.w { "InterstitialAd: expired" }
            cleanup()

            return InterstitialAdShowResult.Expired
        }

        return showLoadedAd(loadedAd)
    }

    @OptIn(ExperimentalAtomicApi::class)
    private suspend fun showLoadedAd(loadedAd: InterstitialAd): InterstitialAdShowResult = suspendCancellableCoroutine { cont ->
        val handled = AtomicBoolean(false)

        cont.invokeOnCancellation { if (handled.compareAndSet(false, true)) cleanup() }
        showWithCallback(loadedAd) { result -> if (handled.compareAndSet(false, true)) cont.resume(result) }
    }

    private fun showWithCallback(loadedAd: InterstitialAd, onResult: (InterstitialAdShowResult) -> Unit) {
        loadedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            // 閉じるまで待たずに、表示が始まった時点で結果を返す。広告を開いたままアプリが終了すると、閉じた通知は届かないため
            override fun onAdShowedFullScreenContent() {
                onResult(InterstitialAdShowResult.Shown)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Napier.w { "InterstitialAd: failed to show. $adError" }
                cleanup()
                onResult(InterstitialAdShowResult.ShowFailed)
            }

            override fun onAdDismissedFullScreenContent() {
                cleanup()
                onResult(InterstitialAdShowResult.Shown)
            }
        }

        loadedAd.show(activity)
    }

    /** ロード済みの広告が失効していれば破棄し、再ロードする。 */
    internal fun discardExpiredAd() {
        if (!loaded || !isLoadedAdExpired()) return

        Napier.d("InterstitialAd: discard expired ad")
        cleanup()
    }

    private fun isLoadedAdExpired(): Boolean = isAdExpired(
        loadedAtElapsedRealtime = loadedAtElapsedRealtime,
        currentElapsedRealtime = SystemClock.elapsedRealtime(),
    )

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
        hasLoadFailed = false
        loadRequestId += 1
    }
}

@Composable
actual fun rememberInterstitialAdState(adUnitId: String, enable: Boolean): InterstitialAdState {
    val context = LocalActivity.current!!
    val interstitialAdState = remember(adUnitId, enable) {
        InterstitialAdStateImpl(context, adUnitId, enable)
    }

    // 状態は Compose の中で作るため Activity からは参照できない。ここで前面への復帰を監視して、失効した広告を入れ替える
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        interstitialAdState.discardExpiredAd()
    }

    DisposableEffect(interstitialAdState) {
        onDispose {
            interstitialAdState.dispose()
        }
    }

    return interstitialAdState
}

package me.matsumo.fanbox.core.ui.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import me.matsumo.fanbox.core.common.PixiViewConfig
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.resume

/** Android のリワード広告ロード状態を管理するクラス。 */
@OptIn(ExperimentalAtomicApi::class)
class RewardAdLoader(
    private val context: Context,
    private val pixiViewConfig: PixiViewConfig,
) {
    private val _rewardAd = MutableStateFlow<RewardedAd?>(null)
    private val _isShowing = MutableStateFlow(false)
    private val isShowingAd = AtomicBoolean(false)
    private val retryController = AdLoadRetryController(adFormatName = "RewardAd")
    val rewardAd = _rewardAd.asStateFlow()
    val isShowing = _isShowing.asStateFlow()

    private var isLoading = false
    private var isAdsSdkInitialized = false
    private var loadRequestId = 0

    fun loadRewardAdIfNeeded(isAdsSdkInitialized: Boolean) {
        this.isAdsSdkInitialized = isAdsSdkInitialized
        loadRewardAdInternal(isRetry = false)
    }

    private fun loadRewardAdInternal(isRetry: Boolean) {
        val hasLoadedAd = _rewardAd.value != null
        val canLoadAds = isAdsSdkInitialized && !hasLoadedAd
        val canStartLoading = canLoadAds && !isLoading
        val shouldSkipLoading = !canStartLoading || isShowingAd.load()

        if (shouldSkipLoading) return

        if (!isRetry) {
            retryController.reset()
        }

        isLoading = true
        val requestId = ++loadRequestId

        val adRequest = AdRequest.Builder().build()
        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                if (requestId != loadRequestId) return

                Napier.d { "onAdLoaded" }
                _rewardAd.value = rewardedAd
                isLoading = false
                retryController.reset()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                if (requestId != loadRequestId) return

                isLoading = false
                retryController.scheduleRetry(
                    failureMessage = error.toString(),
                    retryAction = ::retryLoadRewardAd,
                )
            }
        }

        RewardedAd.load(context, pixiViewConfig.rewardAdUnitId, adRequest, adLoadCallback)
    }

    private fun retryLoadRewardAd() {
        loadRewardAdInternal(isRetry = true)
    }

    suspend fun showRewardAd(activity: Activity): Boolean {
        val rewardedAd = _rewardAd.value

        if (rewardedAd == null) {
            loadRewardAdInternal(isRetry = false)
            return false
        }

        if (!isShowingAd.compareAndSet(false, true)) return false

        _isShowing.value = true

        return suspendCancellableCoroutine { continuation ->
            showLoadedRewardAd(
                activity = activity,
                rewardedAd = rewardedAd,
                continuation = continuation,
            )
        }
    }

    private fun showLoadedRewardAd(
        activity: Activity,
        rewardedAd: RewardedAd,
        continuation: CancellableContinuation<Boolean>,
    ) {
        val showState = RewardAdShowState()

        rewardedAd.fullScreenContentCallback = createFullScreenContentCallback(
            rewardedAd = rewardedAd,
            showState = showState,
            continuation = continuation,
        )

        runCatching {
            rewardedAd.show(activity) {
                showState.recordRewardEarned()
            }
        }.onFailure { error ->
            Napier.e(error) { "RewardAd: failed to show" }
            completeRewardAdShow(
                rewardedAd = rewardedAd,
                rewardResult = showState.completeWithoutReward(),
                continuation = continuation,
            )
        }

        continuation.invokeOnCancellation {
            completeRewardAdShow(
                rewardedAd = rewardedAd,
                rewardResult = showState.completeWithoutReward(),
                continuation = continuation,
            )
        }
    }

    private fun createFullScreenContentCallback(
        rewardedAd: RewardedAd,
        showState: RewardAdShowState,
        continuation: CancellableContinuation<Boolean>,
    ): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Napier.d { "onAdFailedToShowFullScreenContent: ${error.message}" }
                completeRewardAdShow(
                    rewardedAd = rewardedAd,
                    rewardResult = showState.completeWithoutReward(),
                    continuation = continuation,
                )
            }

            override fun onAdShowedFullScreenContent() {
                Napier.d { "onAdShowedFullScreenContent" }
            }

            override fun onAdDismissedFullScreenContent() {
                Napier.d { "onAdDismissedFullScreenContent" }
                completeRewardAdShow(
                    rewardedAd = rewardedAd,
                    rewardResult = showState.completeByDismissal(),
                    continuation = continuation,
                )
            }
        }
    }

    private fun completeRewardAdShow(
        rewardedAd: RewardedAd,
        rewardResult: Boolean?,
        continuation: CancellableContinuation<Boolean>,
    ) {
        if (rewardResult == null) return

        cleanupRewardAd(rewardedAd)

        if (continuation.isActive) continuation.resume(rewardResult)
    }

    private fun cleanupRewardAd(rewardedAd: RewardedAd) {
        rewardedAd.fullScreenContentCallback = null
        _rewardAd.value = null
        isShowingAd.store(false)
        _isShowing.value = false

        loadRewardAdInternal(isRetry = false)
    }
}

package me.matsumo.fanbox.core.ui.ads

import android.app.Activity
import android.content.Context
import androidx.annotation.MainThread
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

/** Android のリワード広告ロード状態を管理するクラス。 */
class RewardAdLoader(
    private val context: Context,
    private val pixiViewConfig: PixiViewConfig,
) {
    private val _rewardAd = MutableStateFlow<RewardedAd?>(null)
    private val _isShowing = MutableStateFlow(false)
    private val _showResult = MutableStateFlow<RewardAdShowResult?>(null)
    private val retryController = AdLoadRetryController(adFormatName = "RewardAd")
    val rewardAd = _rewardAd.asStateFlow()
    val isShowing = _isShowing.asStateFlow()
    val showResult = _showResult.asStateFlow()

    private var isLoading = false
    private var showRequestId = 0L

    fun loadRewardAdIfNeeded(isAdsSdkInitialized: Boolean) {
        if (!isAdsSdkInitialized) return

        loadRewardAdInternal(isRetry = false)
    }

    private fun loadRewardAdInternal(isRetry: Boolean) {
        val hasLoadedAd = _rewardAd.value != null
        val canStartLoading = !hasLoadedAd && !isLoading
        val shouldSkipLoading = !canStartLoading || _isShowing.value

        if (shouldSkipLoading) return

        if (!isRetry) {
            retryController.reset()
        }

        isLoading = true

        val adRequest = AdRequest.Builder().build()
        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Napier.d { "onAdLoaded" }
                _rewardAd.value = rewardedAd
                isLoading = false
                retryController.reset()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
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

    /**
     * ロード済みのリワード広告を表示し、表示要求 ID を返す。
     *
     * 表示要求 ID はメインスレッドで単調増加させる。
     */
    @MainThread
    fun showRewardAd(activity: Activity): Long? {
        val rewardedAd = _rewardAd.value

        if (rewardedAd == null) return null

        if (!_isShowing.compareAndSet(false, true)) return null

        val requestId = ++showRequestId

        showLoadedRewardAd(
            activity = activity,
            rewardedAd = rewardedAd,
            requestId = requestId,
        )

        return requestId
    }

    private fun showLoadedRewardAd(
        activity: Activity,
        rewardedAd: RewardedAd,
        requestId: Long,
    ) {
        val showState = RewardAdShowState()

        rewardedAd.fullScreenContentCallback = createFullScreenContentCallback(
            rewardedAd = rewardedAd,
            showState = showState,
            requestId = requestId,
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
                requestId = requestId,
            )
        }
    }

    private fun createFullScreenContentCallback(
        rewardedAd: RewardedAd,
        showState: RewardAdShowState,
        requestId: Long,
    ): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Napier.d { "onAdFailedToShowFullScreenContent: ${error.message}" }
                completeRewardAdShow(
                    rewardedAd = rewardedAd,
                    rewardResult = showState.completeWithoutReward(),
                    requestId = requestId,
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
                    requestId = requestId,
                )
            }
        }
    }

    private fun completeRewardAdShow(
        rewardedAd: RewardedAd,
        rewardResult: Boolean?,
        requestId: Long,
    ) {
        if (rewardResult == null) return

        // 復元時の orphan 判定が false/null を観測しないよう、showing を落とす前に結果を公開する。
        _showResult.value = RewardAdShowResult(
            requestId = requestId,
            isRewardEarned = rewardResult,
        )
        cleanupRewardAd(rewardedAd)
        loadRewardAdInternal(isRetry = false)
    }

    private fun cleanupRewardAd(rewardedAd: RewardedAd) {
        rewardedAd.fullScreenContentCallback = null
        _rewardAd.value = null
        _isShowing.value = false
    }

    fun consumeShowResult(showResult: RewardAdShowResult) {
        _showResult.compareAndSet(showResult, null)
    }
}

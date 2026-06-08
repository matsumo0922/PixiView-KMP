package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_ad_load_failed
import me.matsumo.fanbox.core.resources.error_ad_load_failed_description
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.theme.LocalAdsSdkInitialized
import me.matsumo.fanbox.core.ui.theme.LocalPixiViewConfig
import me.matsumo.fanbox.core.ui.theme.bold
import org.jetbrains.compose.resources.stringResource

@SuppressLint("MissingPermission")
@Composable
actual fun BannerAdView(modifier: Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val navigationType = LocalNavigationType.current.type
    val isAdsSdkInitialized = LocalAdsSdkInitialized.current
    val pixiViewConfig = LocalPixiViewConfig.current

    var isAdLoadFailed by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        val containerWidthDp = maxWidth.value.toInt()
        val adSize = remember(context, containerWidthDp, configuration.orientation) {
            if (containerWidthDp > 0) {
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, containerWidthDp)
            } else {
                null
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(adSize?.height?.dp ?: 0.dp),
        ) {
            if (adSize != null && isAdsSdkInitialized) {
                val adView = rememberAdViewWithLifecycle(
                    adUnitId = pixiViewConfig.bannerAdUnitId,
                    adSize = adSize,
                    adRequest = AdRequest.Builder().build(),
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isAdLoadFailed = false
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Napier.w(
                                error.toBannerAdLoadFailedLog(
                                    requestedWidthDp = adSize.width,
                                    containerWidthDp = containerWidthDp,
                                    orientation = configuration.orientation,
                                    navigationType = navigationType.name,
                                ),
                            )
                            isAdLoadFailed = true
                        }
                    },
                )

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { adView },
                )
            }

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.Center),
                visible = isAdLoadFailed,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.error_ad_load_failed),
                        style = MaterialTheme.typography.bodyMedium.bold(),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = stringResource(Res.string.error_ad_load_failed_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun rememberAdViewWithLifecycle(
    adUnitId: String,
    adSize: AdSize,
    adRequest: AdRequest = AdRequest.Builder().build(),
    adListener: AdListener = object : AdListener() {},
): AdView {
    val context = LocalContext.current
    val retryController = remember(context, adUnitId, adSize) {
        AdLoadRetryController(adFormatName = "BannerAds")
    }
    val adView = remember(context, adUnitId, adSize) {
        createBannerAdView(
            context = context,
            adUnitId = adUnitId,
            adSize = adSize,
            adRequest = adRequest,
            retryController = retryController,
            adListener = adListener,
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, adView) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                adView.resume()
            }

            override fun onPause(owner: LifecycleOwner) {
                adView.pause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                adView.destroy()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            retryController.reset()
            adView.destroy()
        }
    }

    return adView
}

@SuppressLint("MissingPermission")
private fun createBannerAdView(
    context: Context,
    adUnitId: String,
    adSize: AdSize,
    adRequest: AdRequest,
    retryController: AdLoadRetryController,
    adListener: AdListener,
): AdView {
    val adView = AdView(context)
    adView.setAdSize(adSize)
    adView.adUnitId = adUnitId
    adView.adListener = BannerAdRetryListener(
        adView = adView,
        adRequest = adRequest,
        retryController = retryController,
        delegate = adListener,
    )
    adView.loadAd(adRequest)
    return adView
}

/** バナー広告のロード失敗時に指数バックオフで再試行し、その他の通知は delegate へ委譲する AdListener。 */
private class BannerAdRetryListener(
    private val adView: AdView,
    private val adRequest: AdRequest,
    private val retryController: AdLoadRetryController,
    private val delegate: AdListener,
) : AdListener() {
    override fun onAdLoaded() {
        retryController.reset()
        delegate.onAdLoaded()
    }

    override fun onAdFailedToLoad(error: LoadAdError) {
        delegate.onAdFailedToLoad(error)
        retryController.scheduleRetry(
            failureMessage = error.toString(),
            retryAction = ::reloadAd,
        )
    }

    override fun onAdClicked() = delegate.onAdClicked()

    override fun onAdClosed() = delegate.onAdClosed()

    override fun onAdImpression() = delegate.onAdImpression()

    override fun onAdOpened() = delegate.onAdOpened()

    @SuppressLint("MissingPermission")
    private fun reloadAd() {
        adView.loadAd(adRequest)
    }
}

private fun LoadAdError.toBannerAdLoadFailedLog(
    requestedWidthDp: Int,
    containerWidthDp: Int,
    orientation: Int,
    navigationType: String,
): String {
    val orientationName = when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> "landscape"
        Configuration.ORIENTATION_PORTRAIT -> "portrait"
        else -> "undefined"
    }

    return "BannerAdView: onAdFailedToLoad, " +
        "code=$code, " +
        "domain=$domain, " +
        "message=$message, " +
        "responseInfo=$responseInfo, " +
        "requestedWidthDp=$requestedWidthDp, " +
        "containerWidthDp=$containerWidthDp, " +
        "orientation=$orientationName, " +
        "navigationType=$navigationType"
}

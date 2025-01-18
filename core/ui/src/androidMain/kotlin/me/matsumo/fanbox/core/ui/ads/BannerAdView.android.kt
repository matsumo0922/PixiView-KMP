package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdView
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_ad_load_failed
import me.matsumo.fanbox.core.resources.error_ad_load_failed_description
import me.matsumo.fanbox.core.ui.theme.LocalPixiViewConfig
import me.matsumo.fanbox.core.ui.theme.bold
import org.jetbrains.compose.resources.stringResource

@SuppressLint("MissingPermission")
@Composable
actual fun BannerAdView(modifier: Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val pixiViewConfig = LocalPixiViewConfig.current

    var isAdLoadFailed by remember { mutableStateOf(false) }

    val adSizeHeight = remember {
        val displayMetrics = Resources.getSystem().displayMetrics
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width)
        (adSize.height * displayMetrics.density)
    }

    val adManagerAdView = rememberAdViewWithLifecycle(
        adUnitId = pixiViewConfig.adMobAndroid.bannerAdUnitId,
        adRequest = AdRequest.Builder().build(),
        adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                isAdLoadFailed = true
            }
        },
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(with(density) { adSizeHeight.toDp() }),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { adManagerAdView },
        )

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

@SuppressLint("MissingPermission")
@Composable
fun rememberAdViewWithLifecycle(
    adUnitId: String,
    adRequest: AdRequest = AdRequest.Builder().build(),
    adListener: AdListener = object : AdListener() {},
): AdManagerAdView {
    val context = LocalContext.current
    val adView = remember {
        AdManagerAdView(context).apply {
            val displayMetrics = Resources.getSystem().displayMetrics
            val width = (displayMetrics.widthPixels / displayMetrics.density).toInt()

            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width))

            this.adListener = adListener
            this.adUnitId = adUnitId

            loadAd(adRequest)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
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

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return adView
}

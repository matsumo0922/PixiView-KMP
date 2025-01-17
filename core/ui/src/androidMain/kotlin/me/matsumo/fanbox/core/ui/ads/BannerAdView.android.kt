package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.admanager.AdManagerAdView
import me.matsumo.fanbox.core.ui.theme.LocalPixiViewConfig

@SuppressLint("MissingPermission")
@Composable
actual fun BannerAdView(modifier: Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val pixiViewConfig = LocalPixiViewConfig.current

    val adSizeHeight = remember {
        val displayMetrics = Resources.getSystem().displayMetrics
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width)
        (adSize.height * displayMetrics.density)
    }

    val adManagerAdView = rememberAdViewWithLifecycle(
        adUnitId = pixiViewConfig.adMobAndroid.bannerAdUnitId,
        adRequest = AdRequest.Builder().build(),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(with(density) { adSizeHeight.toDp() })
    ) {
        AndroidView(
            modifier = modifier,
            factory = { adManagerAdView },
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun rememberAdViewWithLifecycle(
    adUnitId: String,
    adRequest: AdRequest = AdRequest.Builder().build(),
): AdManagerAdView {
    val context = LocalContext.current
    val adView = remember {
        AdManagerAdView(context).apply {
            val displayMetrics = Resources.getSystem().displayMetrics
            val width = (displayMetrics.widthPixels / displayMetrics.density).toInt()

            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width))
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

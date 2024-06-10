package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.admanager.AdManagerAdView
import me.matsumo.fanbox.core.ui.theme.LocalPixiViewConfig

@SuppressLint("MissingPermission")
@Composable
actual fun BannerAdView(modifier: Modifier) {
    val pixiViewConfig = LocalPixiViewConfig.current
    val adManagerAdView = rememberAdViewWithLifecycle(
        adUnitId = pixiViewConfig.adMobAndroid.bannerAdUnitId,
        adRequest = AdRequest.Builder().build(),
    )

    AndroidView(
        modifier = modifier,
        factory = { adManagerAdView },
    )
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

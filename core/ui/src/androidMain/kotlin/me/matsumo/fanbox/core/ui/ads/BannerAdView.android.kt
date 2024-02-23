package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.admanager.AdManagerAdView
import me.matsumo.fanbox.core.ui.theme.LocalPixiViewConfig

@SuppressLint("MissingPermission")
@Composable
actual fun BannerAdView(modifier: Modifier) {
    val pixiViewConfig = LocalPixiViewConfig.current

    AndroidView(
        modifier = modifier,
        factory = {
            val displayMetrics = Resources.getSystem().displayMetrics
            val width = (displayMetrics.widthPixels / displayMetrics.density).toInt()

            AdManagerAdView(it).apply {
                setAdSizes(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(it, width))
                adUnitId = pixiViewConfig.adMobAndroid.bannerAdUnitId
            }
        },
        update = {
            it.loadAd(AdRequest.Builder().build())
        }
    )
}

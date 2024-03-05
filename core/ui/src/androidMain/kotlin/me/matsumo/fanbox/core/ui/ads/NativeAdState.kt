package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun rememberNativeAdState(
    adUnit: String,
    adListener: AdListener? = null,
    adOptions: NativeAdOptions? = null
): NativeAdState {
    val context = LocalContext.current
    val activity = (context as? FragmentActivity) ?: error("Activity not found")
    val scope = rememberCoroutineScope()

    return remember(adUnit) {
        NativeAdState(
            activity = activity,
            adUnit = adUnit,
            adListener = adListener,
            adOptions = adOptions,
            scope = scope,
        )
    }
}

@SuppressLint("MissingPermission")
class NativeAdState(
    activity: Activity,
    adUnit: String,
    adListener: AdListener?,
    adOptions: NativeAdOptions?,
    scope: CoroutineScope,
) {
    val nativeAd = MutableStateFlow<NativeAd?>(null)

    init {
        scope.launch(Dispatchers.IO) {
            AdLoader.Builder(activity, adUnit).apply {
                if (adOptions != null) withNativeAdOptions(adOptions)
                if (adListener != null) withAdListener(adListener)

                forNativeAd {
                    if (activity.isDestroyed) {
                        it.destroy()
                        return@forNativeAd
                    }

                    nativeAd.tryEmit(it)
                }
            }
                .build()
                .loadAd(AdRequest.Builder().build())
        }
    }
}

package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.doOnLayout
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import me.matsumo.fanbox.core.ui.databinding.LayoutNativeAdsMediumBinding

@SuppressLint("MissingPermission")
@Composable
fun NativeAdMediumItem(
    nativeAdUnitId: String,
    nativeAdsPreLoader: NativeAdsPreLoader,
    modifier: Modifier = Modifier,
) {
    fun setupNativeAd(binding: LayoutNativeAdsMediumBinding, nativeAd: NativeAd) {
        val adView = binding.root.also { adView ->
            adView.advertiserView = binding.tvAdvertiser
            adView.bodyView = binding.tvBody
            adView.callToActionView = binding.btnCta
            adView.headlineView = binding.tvHeadline
            adView.iconView = binding.ivAppIcon
            adView.priceView = binding.tvPrice
            adView.starRatingView = binding.rtbStars
            adView.storeView = binding.tvStore
            adView.mediaView = binding.mvContent
        }

        nativeAd.advertiser?.let { binding.tvAdvertiser.text = it }
        nativeAd.body?.let { binding.tvBody.text = it }
        nativeAd.callToAction?.let { binding.btnCta.text = it }
        nativeAd.headline?.let { binding.tvHeadline.text = it }
        nativeAd.icon?.let { binding.ivAppIcon.setImageDrawable(it.drawable) }
        nativeAd.price?.let { binding.tvPrice.text = it }
        nativeAd.starRating?.let { binding.rtbStars.rating = it.toFloat() }
        nativeAd.store?.let { binding.tvStore.text = it }

        binding.tvAdvertiser.visibility = if (nativeAd.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE

        adView.setNativeAd(nativeAd)
    }

    val context = LocalContext.current

    var nativeAdKey by rememberSaveable { mutableIntStateOf(-1) }
    var isAdLoaded by rememberSaveable { mutableStateOf(false) }

    val titleTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val bodyTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val buttonColor = MaterialTheme.colorScheme.primary.toArgb()
    val buttonTextColor = MaterialTheme.colorScheme.onPrimary.toArgb()

    DisposableEffect(!isAdLoaded) {
        if (!isAdLoaded) {
            nativeAdKey = nativeAdsPreLoader.getKey() ?: -1
            isAdLoaded = true
        }

        onDispose {
            nativeAdsPreLoader.popAd(nativeAdKey)
        }
    }

    if (isAdLoaded) {
        Card(
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        ) {
            AndroidViewBinding(
                modifier = Modifier.fillMaxWidth(),
                factory = { inflater, parent, attachToParent ->
                    val binding = LayoutNativeAdsMediumBinding.inflate(inflater, parent, attachToParent)

                    binding.tvAdvertiser.setTextColor(bodyTextColor)
                    binding.tvBody.setTextColor(bodyTextColor)
                    binding.btnCta.setTextColor(buttonTextColor)
                    binding.btnCta.backgroundTintList = ColorStateList.valueOf(buttonColor)
                    binding.tvHeadline.setTextColor(titleTextColor)
                    binding.tvPrice.setTextColor(bodyTextColor)
                    binding.tvStore.setTextColor(bodyTextColor)
                    binding.tvAd.setTextColor(bodyTextColor)

                    binding
                },
                update = {
                    this.root.doOnLayout {
                        if (nativeAdKey != -1) {
                            nativeAdsPreLoader.getAd(nativeAdKey)?.let {
                                setupNativeAd(this, it)
                                return@doOnLayout
                            }
                        }

                        val adLoader = AdLoader.Builder(context, nativeAdUnitId)
                            .forNativeAd { setupNativeAd(this, it) }
                            .withNativeAdOptions(NativeAdOptions.Builder().build())
                            .build()

                        adLoader.loadAd(AdRequest.Builder().build())
                    }
                },
            )
        }
    }
}

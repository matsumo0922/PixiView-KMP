package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import com.google.android.gms.ads.nativead.NativeAd
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.core.ui.databinding.LayoutNativeAdsMediumBinding
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
@Composable
actual fun NativeAdView(
    key: String,
    modifier: Modifier,
) {
    val nativeAdsPreLoader = koinInject<NativeAdsPreLoader>()
    var nativeAd: NativeAd? = null

    fun setupNativeAd(binding: LayoutNativeAdsMediumBinding, nativeAd: NativeAd) {
        if (binding.tvPrice.text.isNotBlank()) return

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
        nativeAd.mediaContent?.let { binding.mvContent.mediaContent = it }

        binding.tvAdvertiser.visibility = if (nativeAd.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.mvContent.isVisible = nativeAd.mediaContent != null
        binding.mvContent.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                if (child is ImageView) {
                    child.adjustViewBounds = true
                }
            }

            override fun onChildViewRemoved(parent: View, child: View) = Unit
        })

        adView.setNativeAd(nativeAd)
        adView.requestLayoutWithDelay(500L)

        Napier.d("NativeAdView: setupNativeAd, ${binding.tvPrice.text}, $key")
    }

    val titleTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val bodyTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val buttonColor = MaterialTheme.colorScheme.primary.toArgb()
    val buttonTextColor = MaterialTheme.colorScheme.onPrimary.toArgb()

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
                nativeAd = nativeAdsPreLoader.getNativeAd(key)
                nativeAd?.let { nativeAd -> setupNativeAd(this, nativeAd) }
            }
        )
    }
}

private fun View.requestLayoutWithDelay(delayMillis: Long) {
    post {
        val composeView = parent.findAndroidComposeViewParent()
        if (composeView == null) {
            postDelayed(delayMillis) {
                parent.findAndroidComposeViewParent()?.requestLayout()
            }
        } else {
            composeView.requestLayout()
        }
    }
}

private fun ViewParent?.findAndroidComposeViewParent(): ViewParent? = when {
    this != null && this::class.java.simpleName == "AndroidComposeView" -> this
    this != null -> this.parent.findAndroidComposeViewParent()
    else -> null
}

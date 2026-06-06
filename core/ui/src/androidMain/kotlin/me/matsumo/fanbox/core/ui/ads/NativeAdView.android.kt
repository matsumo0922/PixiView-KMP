package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import com.google.android.gms.ads.nativead.NativeAd
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.core.ui.R
import me.matsumo.fanbox.core.ui.databinding.LayoutNativeAdsMediumBinding
import me.matsumo.fanbox.core.ui.theme.LocalAdsSdkInitialized
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
@Composable
actual fun NativeAdView(
    key: String,
    modifier: Modifier,
) {
    val isAdsSdkInitialized = LocalAdsSdkInitialized.current

    if (!isAdsSdkInitialized) {
        return
    }

    val nativeAdsPreLoader = koinInject<NativeAdsPreLoader>()
    val nativeAdInventoryVersion by nativeAdsPreLoader.nativeAdInventoryVersion.collectAsState()
    var nativeAd by remember(key) { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(
        key1 = key,
        key2 = nativeAdInventoryVersion,
    ) {
        nativeAd = nativeAd ?: nativeAdsPreLoader.getNativeAd(key)
    }

    DisposableEffect(
        key1 = key,
        key2 = nativeAdsPreLoader,
    ) {
        onDispose {
            nativeAdsPreLoader.popAd(key = key)
        }
    }

    val titleTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val bodyTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val buttonColor = MaterialTheme.colorScheme.primary.toArgb()
    val buttonTextColor = MaterialTheme.colorScheme.onPrimary.toArgb()
    val colorPalette = NativeAdViewColorPalette(
        titleTextColor = titleTextColor,
        bodyTextColor = bodyTextColor,
        buttonColor = buttonColor,
        buttonTextColor = buttonTextColor,
    )

    Card(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        AndroidViewBinding(
            modifier = Modifier.fillMaxWidth(),
            factory = { inflater, parent, attachToParent ->
                createNativeAdBinding(
                    inflater = inflater,
                    parent = parent,
                    attachToParent = attachToParent,
                    colorPalette = colorPalette,
                )
            },
            update = {
                nativeAd?.let { loadedNativeAd ->
                    setupNativeAd(
                        nativeAd = loadedNativeAd,
                        key = key,
                    )
                }
            },
        )
    }
}

private fun createNativeAdBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean,
    colorPalette: NativeAdViewColorPalette,
): LayoutNativeAdsMediumBinding {
    val binding = LayoutNativeAdsMediumBinding.inflate(inflater, parent, attachToParent)

    binding.applyColors(colorPalette = colorPalette)

    return binding
}

/** ネイティブ広告ビューへ適用する色設定。 */
@Immutable
private data class NativeAdViewColorPalette(
    val titleTextColor: Int,
    val bodyTextColor: Int,
    val buttonColor: Int,
    val buttonTextColor: Int,
)

private fun LayoutNativeAdsMediumBinding.applyColors(
    colorPalette: NativeAdViewColorPalette,
) {
    tvAdvertiser.setTextColor(colorPalette.bodyTextColor)
    tvBody.setTextColor(colorPalette.bodyTextColor)
    btnCta.setTextColor(colorPalette.buttonTextColor)
    btnCta.backgroundTintList = ColorStateList.valueOf(colorPalette.buttonColor)
    tvHeadline.setTextColor(colorPalette.titleTextColor)
    tvPrice.setTextColor(colorPalette.bodyTextColor)
    tvStore.setTextColor(colorPalette.bodyTextColor)
    tvAd.setTextColor(colorPalette.bodyTextColor)
}

private fun LayoutNativeAdsMediumBinding.setupNativeAd(
    nativeAd: NativeAd,
    key: String,
) {
    if (isSameNativeAdBound(nativeAd = nativeAd)) return

    registerNativeAdAssetViews()
    bindNativeAdAssets(nativeAd = nativeAd)
    adjustMediaImageBounds()

    root.setNativeAd(nativeAd)
    root.setTag(R.id.tag_native_ad_view_bound_ad, nativeAd)
    root.requestLayoutWithDelay(500L)

    Napier.d("NativeAdView: setupNativeAd, ${tvPrice.text}, $key")
}

private fun LayoutNativeAdsMediumBinding.isSameNativeAdBound(nativeAd: NativeAd): Boolean {
    val boundNativeAd = root.getTag(R.id.tag_native_ad_view_bound_ad) as? NativeAd
    return boundNativeAd === nativeAd
}

private fun LayoutNativeAdsMediumBinding.registerNativeAdAssetViews() {
    root.advertiserView = tvAdvertiser
    root.bodyView = tvBody
    root.callToActionView = btnCta
    root.headlineView = tvHeadline
    root.iconView = ivAppIcon
    root.priceView = tvPrice
    root.starRatingView = rtbStars
    root.storeView = tvStore
    root.mediaView = mvContent
}

private fun LayoutNativeAdsMediumBinding.bindNativeAdAssets(nativeAd: NativeAd) {
    tvAdvertiser.text = nativeAd.advertiser.orEmpty()
    tvBody.text = nativeAd.body.orEmpty()
    btnCta.text = nativeAd.callToAction.orEmpty()
    tvHeadline.text = nativeAd.headline.orEmpty()
    ivAppIcon.setImageDrawable(nativeAd.icon?.drawable)
    tvPrice.text = nativeAd.price.orEmpty()
    rtbStars.rating = nativeAd.starRating?.toFloat() ?: 0f
    tvStore.text = nativeAd.store.orEmpty()
    mvContent.mediaContent = nativeAd.mediaContent

    tvAdvertiser.visibility = if (nativeAd.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE
    mvContent.isVisible = nativeAd.mediaContent != null
}

private fun LayoutNativeAdsMediumBinding.adjustMediaImageBounds() {
    mvContent.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewAdded(parent: View, child: View) {
            if (child is ImageView) {
                child.adjustViewBounds = true
            }
        }

        override fun onChildViewRemoved(parent: View, child: View) = Unit
    })
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

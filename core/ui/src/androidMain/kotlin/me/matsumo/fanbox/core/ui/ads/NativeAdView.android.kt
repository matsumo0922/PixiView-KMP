package me.matsumo.fanbox.core.ui.ads

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
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

    Card(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        if (isAdsSdkInitialized) {
            NativeAdViewContent(key = key)
        } else {
            NativeAdViewPlaceholder()
        }
    }
}

@Composable
private fun NativeAdViewContent(
    key: String,
    modifier: Modifier = Modifier,
) {
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
            nativeAdsPreLoader.releaseAd(key = key)
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

    AndroidViewBinding(
        modifier = modifier.fillMaxWidth(),
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

@Composable
private fun NativeAdViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NativeAdPlaceholderHeight),
    )
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
    adjustMediaViewAspectRatio(nativeAd = nativeAd)

    root.setNativeAd(nativeAd)
    root.setTag(R.id.tag_native_ad_view_bound_ad, nativeAd)

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

private fun LayoutNativeAdsMediumBinding.adjustMediaViewAspectRatio(nativeAd: NativeAd) {
    val layoutParams = mvContent.layoutParams as? ConstraintLayout.LayoutParams ?: return
    val mediaAspectRatio = nativeAd.mediaContent?.aspectRatio ?: 0f
    val hasValidAspectRatio = mediaAspectRatio > 0f

    layoutParams.dimensionRatio = if (hasValidAspectRatio) "H,$mediaAspectRatio:1" else DefaultMediaAspectRatio
    mvContent.layoutParams = layoutParams
}

/** SDK 初期化前にネイティブ広告枠として確保する高さ。 */
private val NativeAdPlaceholderHeight = 60.dp

/** メディアアスペクト比を取得できない場合に使用する既定比率（横:縦）。 */
private const val DefaultMediaAspectRatio = "H,16:9"

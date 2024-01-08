package me.matsumo.fanbox.core.ui.extensition

import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.DrawableImage
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalCoilApi::class)
@Composable
actual fun ImageResource.asCoilImage(): Image {
    return getDrawable(LocalContext.current)?.asCoilImage() ?: error("can't read Drawable of $this")
}

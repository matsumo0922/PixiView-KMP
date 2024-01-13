package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import dev.icerock.moko.resources.ImageResource

@OptIn(ExperimentalCoilApi::class)
@Composable
actual fun ImageResource.asCoilImage(): Image {
    return getDrawable(LocalContext.current)?.asCoilImage() ?: error("can't read Drawable of $this")
}

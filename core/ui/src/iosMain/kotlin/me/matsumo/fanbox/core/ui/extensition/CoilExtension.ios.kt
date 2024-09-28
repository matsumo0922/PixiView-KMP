package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asSkiaBitmap
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource

@OptIn(ExperimentalCoilApi::class)
@Composable
actual fun DrawableResource.asCoilImage(): Image {
    return imageResource(this).asSkiaBitmap().asCoilImage()
}

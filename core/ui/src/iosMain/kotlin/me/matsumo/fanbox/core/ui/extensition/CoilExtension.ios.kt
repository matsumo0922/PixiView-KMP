package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asSkiaBitmap
import coil3.Image
import coil3.asImage
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource

@Composable
actual fun DrawableResource.asCoilImage(): Image {
    return imageResource(this).asSkiaBitmap().asImage()
}

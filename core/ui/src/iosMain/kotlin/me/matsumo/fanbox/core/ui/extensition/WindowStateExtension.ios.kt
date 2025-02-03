package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenSizeDp(): DpSize {
    val density = LocalDensity.current
    val containerSize = LocalWindowInfo.current.containerSize

    return with(density) {
        DpSize(
            width = containerSize.width.toDp(),
            height = containerSize.height.toDp(),
        )
    }
}

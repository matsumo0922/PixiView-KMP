package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
actual fun getScreenSizeDp(): DpSize {
    return DpSize(
        width = LocalConfiguration.current.screenWidthDp.dp,
        height = LocalConfiguration.current.screenHeightDp.dp,
    )
}

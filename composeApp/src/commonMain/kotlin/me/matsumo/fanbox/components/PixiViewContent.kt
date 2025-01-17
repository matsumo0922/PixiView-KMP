package me.matsumo.fanbox.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.PixiViewNavHost

@Composable
internal fun PixiViewContent(
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        PixiViewNavHost(
            modifier = Modifier.fillMaxSize(),
        )
    }
}

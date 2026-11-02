package me.matsumo.fanbox.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.PixiViewNavHost
import me.matsumo.fanbox.core.ui.navigation.PixiViewNavigator

@Composable
internal fun PixiViewContent(
    navigator: PixiViewNavigator,
    onPostDetailClose: suspend () -> Unit,
    onLibraryHomeVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        PixiViewNavHost(
            modifier = Modifier.fillMaxSize(),
            navigator = navigator,
            onPostDetailClose = onPostDetailClose,
            onLibraryHomeVisibilityChange = onLibraryHomeVisibilityChange,
        )
    }
}

package me.matsumo.fanbox.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import me.matsumo.fanbox.PixiViewNavHost
import me.matsumo.fanbox.core.ui.component.sheet.BottomSheetNavigator

@Composable
internal fun PixiViewContent(
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        PixiViewNavHost(
            modifier = Modifier.fillMaxSize(),
            bottomSheetNavigator = bottomSheetNavigator,
            navController = navController,
        )
    }
}

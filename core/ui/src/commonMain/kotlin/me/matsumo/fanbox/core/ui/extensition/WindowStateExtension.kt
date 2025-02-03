package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.DpSize

enum class PixiViewNavigationType {
    BottomNavigation,
    NavigationRail,
    PermanentNavigationDrawer,
}

enum class PixiViewContentType {
    ListOnly,
    ListAndDetail,
}

@Composable
expect fun getScreenSizeDp(): DpSize

@Immutable
data class NavigationType(
    val type: PixiViewNavigationType = PixiViewNavigationType.BottomNavigation,
)

val LocalNavigationType = staticCompositionLocalOf { NavigationType() }

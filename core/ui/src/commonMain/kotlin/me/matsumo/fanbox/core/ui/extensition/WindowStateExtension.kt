package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

enum class PixiViewNavigationType {
    BottomNavigation,
    NavigationRail,
    PermanentNavigationDrawer,
}

enum class PixiViewContentType {
    ListOnly,
    ListAndDetail,
}

@Immutable
data class NavigationType(
    val type: PixiViewNavigationType = PixiViewNavigationType.BottomNavigation,
)

val LocalNavigationType = staticCompositionLocalOf { NavigationType() }

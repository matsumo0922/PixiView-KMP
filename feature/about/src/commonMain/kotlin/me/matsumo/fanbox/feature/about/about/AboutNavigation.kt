package me.matsumo.fanbox.feature.about.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val AboutRoute = "about"

fun Navigator.navigateToAbout() {
    this.navigate(AboutRoute)
}

fun RouteBuilder.aboutScreen(
    navigateToVersionHistory: () -> Unit,
    navigateToDonate: () -> Unit,
    terminate: () -> Unit,
) {
    scene(
        route = AboutRoute,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        AboutRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToVersionHistory = navigateToVersionHistory,
            navigateToDonate = navigateToDonate,
            terminate = terminate,
        )
    }
}

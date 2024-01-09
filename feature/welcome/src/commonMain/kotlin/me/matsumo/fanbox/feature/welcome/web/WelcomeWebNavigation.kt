package me.matsumo.fanbox.feature.welcome.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.feature.welcome.top.WelcomeTopScreen
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val WelcomeWebRoute = "welcomeWeb"

fun Navigator.navigateToWelcomeWeb() {
    this.navigate(WelcomeWebRoute)
}

fun RouteBuilder.welcomeWebScreen(
    terminate: () -> Unit,
) {
    scene(
        route = WelcomeWebRoute,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        WelcomeWebScreen(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}

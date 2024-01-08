package me.matsumo.fanbox.feature.welcome.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val WelcomeTopRoute = "welcomeTop"

fun Navigator.navigateToWelcomeTop() {
    this.navigate(WelcomeTopRoute)
}

fun RouteBuilder.welcomeTopScreen(
    navigateToWelcomePlus: () -> Unit,
) {
    scene(
        route = WelcomeTopRoute,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        WelcomeTopScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToWelcomePlus = navigateToWelcomePlus,
        )
    }
}

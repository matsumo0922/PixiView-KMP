package me.matsumo.fanbox.feature.welcome.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val WelcomeTopRoute = "welcomeTop"

fun Navigator.navigateToWelcomeTop() {
    this.navigate(WelcomeTopRoute)
}

fun RouteBuilder.welcomeTopScreen(
    navigateToWelcomeLogin: () -> Unit,
    navigateToForIos: suspend (SimpleAlertContents) -> Unit,
) {
    scene(WelcomeTopRoute) {
        WelcomeTopScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToWelcomeLogin = navigateToWelcomeLogin,
            navigateToForIos = navigateToForIos,
        )
    }
}

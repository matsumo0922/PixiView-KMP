package me.matsumo.fanbox.feature.welcome.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val WelcomeWebRoute = "welcomeWeb"

fun Navigator.navigateToWelcomeWeb() {
    this.navigate(WelcomeWebRoute)
}

fun RouteBuilder.welcomeWebScreen(
    navigateToLoginAlert: suspend (SimpleAlertContents) -> Unit,
    terminate: () -> Unit,
) {
    scene(WelcomeWebRoute) {
        WelcomeWebScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToLoginAlert = navigateToLoginAlert,
            terminate = terminate,
        )
    }
}

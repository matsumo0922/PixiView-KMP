package me.matsumo.fanbox.feature.welcome.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val WelcomeWebRoute = "welcomeWeb"

fun NavController.navigateToWelcomeWeb() {
    this.navigate(WelcomeWebRoute)
}

fun NavGraphBuilder.welcomeWebScreen(
    navigateToLoginAlert: suspend (SimpleAlertContents) -> Unit,
    terminate: () -> Unit,
) {
    composable(WelcomeWebRoute) {
        WelcomeWebScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToLoginAlert = navigateToLoginAlert,
            terminate = terminate,
        )
    }
}

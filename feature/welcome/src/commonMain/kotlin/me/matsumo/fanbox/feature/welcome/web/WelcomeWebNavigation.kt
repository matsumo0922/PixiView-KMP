package me.matsumo.fanbox.feature.welcome.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fanbox.core.model.SimpleAlertContents

const val WelcomeWebRoute = "welcomeWeb"

fun NavController.navigateToWelcomeWeb() {
    this.navigateWithLog(WelcomeWebRoute)
}

fun NavGraphBuilder.welcomeWebScreen(
    navigateToLoginAlert: (SimpleAlertContents) -> Unit,
    navigateToLoginDebugAlert: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    composable(WelcomeWebRoute) {
        WelcomeWebScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToLoginAlert = navigateToLoginAlert,
            navigateToLoginDebugAlert = navigateToLoginDebugAlert,
            terminate = terminate,
        )
    }
}

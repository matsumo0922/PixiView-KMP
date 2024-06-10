package me.matsumo.fanbox.feature.welcome.login

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val WelcomeLoginRoute = "welcomeLogin"

fun NavController.navigateToWelcomeLogin() {
    this.navigateWithLog(WelcomeLoginRoute)
}

fun NavGraphBuilder.welcomeLoginScreen(
    navigateToLoginScreen: () -> Unit,
    navigateToWelcomePermission: () -> Unit,
) {
    composable(WelcomeLoginRoute) {
        WelcomeLoginScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToLoginScreen = navigateToLoginScreen,
            navigateToWelcomePermission = navigateToWelcomePermission,
        )
    }
}

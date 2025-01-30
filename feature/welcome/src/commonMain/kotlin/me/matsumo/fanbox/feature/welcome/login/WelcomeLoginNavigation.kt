package me.matsumo.fanbox.feature.welcome.login

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

fun NavGraphBuilder.welcomeLoginScreen(
    navigateToLoginScreen: () -> Unit,
    navigateToHome: () -> Unit,
) {
    composable<Destination.WelcomeLogin> {
        WelcomeLoginScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToLoginScreen = navigateToLoginScreen,
            navigateToHome = navigateToHome,
        )
    }
}

package me.matsumo.fanbox.feature.welcome.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

fun NavGraphBuilder.welcomeTopScreen(
    navigateToWelcomeLogin: () -> Unit,
) {
    composable<Destination.WelcomeTop> {
        WelcomeTopScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToWelcomeLogin = navigateToWelcomeLogin,
        )
    }
}

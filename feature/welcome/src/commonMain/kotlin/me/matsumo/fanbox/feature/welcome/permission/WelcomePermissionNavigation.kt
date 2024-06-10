package me.matsumo.fanbox.feature.welcome.permission

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val WelcomePermissionRoute = "welcomePermission"

fun NavController.navigateToWelcomePermission() {
    this.navigate(WelcomePermissionRoute)
}

fun NavGraphBuilder.welcomePermissionScreen(
    navigateToHome: () -> Unit,
) {
    composable(WelcomePermissionRoute) {
        WelcomePermissionScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToHome = navigateToHome,
        )
    }
}

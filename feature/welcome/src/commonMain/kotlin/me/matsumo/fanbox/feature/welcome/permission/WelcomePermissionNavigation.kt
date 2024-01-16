package me.matsumo.fanbox.feature.welcome.permission

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val WelcomePermissionRoute = "welcomePermission"

fun Navigator.navigateToWelcomePermission() {
    this.navigate(WelcomePermissionRoute)
}

fun RouteBuilder.welcomePermissionScreen(
    navigateToHome: () -> Unit,
) {
    scene(WelcomePermissionRoute) {
        WelcomePermissionScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToHome = navigateToHome,
        )
    }
}

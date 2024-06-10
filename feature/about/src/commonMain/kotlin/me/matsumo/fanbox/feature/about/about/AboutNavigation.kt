package me.matsumo.fanbox.feature.about.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val AboutRoute = "about"

fun NavController.navigateToAbout() {
    this.navigate(AboutRoute)
}

fun NavGraphBuilder.aboutScreen(
    navigateToVersionHistory: () -> Unit,
    navigateToDonate: () -> Unit,
    terminate: () -> Unit,
) {
    composable(AboutRoute) {
        AboutRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToVersionHistory = navigateToVersionHistory,
            navigateToDonate = navigateToDonate,
            terminate = terminate,
        )
    }
}

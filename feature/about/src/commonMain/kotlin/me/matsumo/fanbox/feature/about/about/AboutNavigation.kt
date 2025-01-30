package me.matsumo.fanbox.feature.about.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

fun NavGraphBuilder.aboutScreen(
    navigateTo: (Destination) -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.About> {
        AboutRoute(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}

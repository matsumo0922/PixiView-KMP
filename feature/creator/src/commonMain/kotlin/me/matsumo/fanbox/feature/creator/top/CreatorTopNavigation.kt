package me.matsumo.fanbox.feature.creator.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.core.ui.customNavTypes

fun NavGraphBuilder.creatorTopScreen(
    navigateTo: (Destination) -> Unit,
    navigateToAlertDialog: (SimpleAlertContents, () -> Unit, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.CreatorTop>(
        deepLinks = listOf(
            navDeepLink { uriPattern = "https://www.fanbox.cc/@{creatorId}" },
            navDeepLink { uriPattern = "https://{creatorId}.fanbox.cc/" },
            navDeepLink { uriPattern = "https://{creatorId}.fanbox.cc" },
        ),
        typeMap = customNavTypes,
    ) {
        CreatorTopRoute(
            modifier = Modifier.fillMaxSize(),
            isPosts = it.toRoute<Destination.CreatorTop>().isPosts,
            navigateTo = navigateTo,
            navigateToAlertDialog = navigateToAlertDialog,
            terminate = terminate,
        )
    }
}

package me.matsumo.fanbox.feature.library.discovery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val LIBRARY_DISCOVERY_ROUTE = "libraryDiscovery"

fun NavController.navigateToLibraryDiscovery(
    navOptions: NavOptions? = null,
) {
    this.navigateWithLog(LIBRARY_DISCOVERY_ROUTE, navOptions)
}

fun NavGraphBuilder.libraryDiscoveryScreen(openDrawer: () -> Unit, navigateTo: (Destination) -> Unit) {
    composable(
        route = LIBRARY_DISCOVERY_ROUTE,
    ) {
        LibraryDiscoveryRoute(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            navigateTo = navigateTo,
        )
    }
}

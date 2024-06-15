package me.matsumo.fanbox.feature.library.discovery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val LibraryDiscoveryRoute = "libraryDiscovery"

fun NavController.navigateToLibraryDiscovery(navOptions: NavOptions? = null) {
    this.navigateWithLog(LibraryDiscoveryRoute, navOptions)
}

fun NavGraphBuilder.libraryDiscoveryScreen(
    openDrawer: () -> Unit,
    navigateToPostSearch: () -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
) {
    composable(
        route = LibraryDiscoveryRoute,
    ) {
        LibraryDiscoveryRoute(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            navigateToPostSearch = navigateToPostSearch,
            navigateToCreatorPosts = navigateToCreatorPosts,
        )
    }
}

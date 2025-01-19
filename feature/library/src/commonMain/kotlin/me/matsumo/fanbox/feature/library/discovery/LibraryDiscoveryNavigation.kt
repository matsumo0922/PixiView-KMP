package me.matsumo.fanbox.feature.library.discovery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

const val LibraryDiscoveryRoute = "libraryDiscovery"

fun NavController.navigateToLibraryDiscovery(navOptions: NavOptions? = null) {
    this.navigateWithLog(LibraryDiscoveryRoute, navOptions)
}

fun NavGraphBuilder.libraryDiscoveryScreen(
    openDrawer: () -> Unit,
    navigateToPostSearch: () -> Unit,
    navigateToPostByCreatorSearch: (FanboxCreatorId) -> Unit,
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    navigateToBillingPlus: (String?) -> Unit,
) {
    composable(
        route = LibraryDiscoveryRoute,
    ) {
        LibraryDiscoveryRoute(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            navigateToPostSearch = navigateToPostSearch,
            navigateToPostByCreatorSearch = navigateToPostByCreatorSearch,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToBillingPlus = navigateToBillingPlus,
        )
    }
}

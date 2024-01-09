package me.matsumo.fanbox.feature.library.discovery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val LibraryDiscoveryRoute = "libraryDiscovery"

fun Navigator.navigateToLibraryDiscovery(navOptions: NavOptions? = null) {
    this.navigate(LibraryDiscoveryRoute, navOptions)
}

fun RouteBuilder.libraryDiscoveryScreen(
    openDrawer: () -> Unit,
    navigateToPostSearch: () -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
) {
    scene(
        route = LibraryDiscoveryRoute,
        navTransition = NavigateAnimation.Library.transition
    ) {
        LibraryDiscoveryRoute(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            navigateToPostSearch = navigateToPostSearch,
            navigateToCreatorPosts = navigateToCreatorPosts,
        )
    }
}

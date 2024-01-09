package me.matsumo.fanbox.feature.library.message

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val LibraryMessageRoute = "libraryMessage"

fun Navigator.navigateToLibraryMessage(navOptions: NavOptions? = null) {
    this.navigate(LibraryMessageRoute, navOptions)
}

fun RouteBuilder.libraryMessageScreen(
    openDrawer: () -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
) {
    scene(
        route = LibraryMessageRoute,
        navTransition = NavigateAnimation.Library.transition
    ) {
        LibraryMessageRoute(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            navigateToCreatorPosts = navigateToCreatorPosts,
        )
    }
}

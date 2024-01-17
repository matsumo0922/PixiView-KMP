package me.matsumo.fanbox.feature.library.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val LibraryHomeRoute = "libraryHome"

fun Navigator.navigateToLibraryHome(navOptions: NavOptions? = null) {
    this.navigate(LibraryHomeRoute, navOptions)
}

fun RouteBuilder.libraryHomeScreen(
    openDrawer: () -> Unit,
    navigateToPostDetailFromHome: (PostId) -> Unit,
    navigateToPostDetailFromSupported: (PostId) -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToCreatorPlans: (CreatorId) -> Unit,
    navigateToCancelPlus: (SimpleAlertContents) -> Unit,
) {
    scene(
        route = LibraryHomeRoute,
        navTransition = NavigateAnimation.Library.transition
    ) {
        LibraryHomeScreen(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            navigateToPostDetailFromHome = navigateToPostDetailFromHome,
            navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToCancelPlus = navigateToCancelPlus,
        )
    }
}

package me.matsumo.fanbox.feature.library.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents

const val LibraryHomeRoute = "libraryHome"

fun NavController.navigateToLibraryHome(navOptions: NavOptions? = null) {
    this.navigate(LibraryHomeRoute, navOptions)
}

fun NavGraphBuilder.libraryHomeScreen(
    openDrawer: () -> Unit,
    navigateToPostDetailFromHome: (PostId) -> Unit,
    navigateToPostDetailFromSupported: (PostId) -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToCreatorPlans: (CreatorId) -> Unit,
    navigateToSimpleAlert: (SimpleAlertContents) -> Unit,
) {
    composable(
        route = LibraryHomeRoute
    ) {
        LibraryHomeScreen(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            navigateToPostDetailFromHome = navigateToPostDetailFromHome,
            navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToSimpleAlert = navigateToSimpleAlert,
        )
    }
}

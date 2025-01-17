package me.matsumo.fanbox.feature.library.message

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

const val LibraryMessageRoute = "libraryMessage"

fun NavController.navigateToLibraryMessage(navOptions: NavOptions? = null) {
    this.navigateWithLog(LibraryMessageRoute, navOptions)
}

fun NavGraphBuilder.libraryMessageScreen(
    openDrawer: () -> Unit,
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
) {
    composable(
        route = LibraryMessageRoute,
    ) {
        LibraryMessageRoute(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            navigateToCreatorPosts = navigateToCreatorPosts,
        )
    }
}

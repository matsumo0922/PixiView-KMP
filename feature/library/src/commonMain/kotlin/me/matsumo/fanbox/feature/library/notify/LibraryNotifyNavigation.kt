package me.matsumo.fanbox.feature.library.notify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val LibraryNotifyRoute = "libraryNotify"

fun NavController.navigateToLibraryNotify(navOptions: NavOptions? = null) {
    this.navigate(LibraryNotifyRoute, navOptions)
}

fun NavGraphBuilder.libraryNotifyScreen(
    openDrawer: () -> Unit,
    navigateToPostDetail: (PostId) -> Unit,
) {
    composable(
        route = LibraryNotifyRoute,
    ) {
        LibraryNotifyRoute(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            openDrawer = openDrawer,
            navigateToPostDetail = navigateToPostDetail,
        )
    }
}

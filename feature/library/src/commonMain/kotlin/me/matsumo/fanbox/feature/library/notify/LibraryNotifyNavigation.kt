package me.matsumo.fanbox.feature.library.notify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

const val LibraryNotifyRoute = "libraryNotify"

fun NavController.navigateToLibraryNotify(navOptions: NavOptions? = null) {
    this.navigateWithLog(LibraryNotifyRoute, navOptions)
}

fun NavGraphBuilder.libraryNotifyScreen(
    openDrawer: () -> Unit,
    navigateToPostDetail: (FanboxPostId) -> Unit,
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

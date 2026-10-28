package me.matsumo.fanbox.feature.library.message

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val LIBRARY_MESSAGE_ROUTE = "libraryMessage"

fun NavController.navigateToLibraryMessage(
    navOptions: NavOptions? = null,
) {
    this.navigateWithLog(LIBRARY_MESSAGE_ROUTE, navOptions)
}

fun NavGraphBuilder.libraryMessageScreen(openDrawer: () -> Unit, navigateTo: (Destination) -> Unit) {
    composable(
        route = LIBRARY_MESSAGE_ROUTE,
    ) {
        LIBRARY_MESSAGE_ROUTE(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            navigateTo = navigateTo,
        )
    }
}

package me.matsumo.fanbox.feature.library.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val LIBRARY_HOME_ROUTE = "libraryHome"

fun NavController.navigateToLibraryHome(
    navOptions: NavOptions? = null,
) {
    this.navigateWithLog(LIBRARY_HOME_ROUTE, navOptions)
}

fun NavGraphBuilder.libraryHomeScreen(openDrawer: () -> Unit, navigateTo: (Destination) -> Unit) {
    composable(
        route = LIBRARY_HOME_ROUTE,
    ) {
        LibraryHomeScreen(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            navigateTo = navigateTo,
        )
    }
}

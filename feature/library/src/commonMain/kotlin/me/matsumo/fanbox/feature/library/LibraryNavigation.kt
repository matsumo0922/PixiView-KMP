package me.matsumo.fanbox.feature.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

fun NavGraphBuilder.libraryScreen(
    navHostController: NavHostController,
    navigateTo: (Destination) -> Unit,
) {
    composable<Destination.Library> {
        LibraryScreen(
            modifier = Modifier.fillMaxSize(),
            navHostController = navHostController,
            navigateTo = navigateTo,
        )
    }
}

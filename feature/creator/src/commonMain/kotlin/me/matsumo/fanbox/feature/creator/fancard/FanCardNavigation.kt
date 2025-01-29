package me.matsumo.fanbox.feature.creator.fancard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

fun NavGraphBuilder.fanCardScreen(
    terminate: () -> Unit,
) {
    composable<Destination.FanCard> {
        FanCardRoute(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}

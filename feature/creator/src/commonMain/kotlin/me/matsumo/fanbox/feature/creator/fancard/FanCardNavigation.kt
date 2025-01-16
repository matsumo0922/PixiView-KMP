package me.matsumo.fanbox.feature.creator.fancard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

const val FanCardId = "fanCardId"
const val FanCardRoute = "fanCard/{$FanCardId}"

fun NavController.navigateToFanCard(creatorId: FanboxCreatorId) {
    this.navigateWithLog("fanCard/$creatorId")
}

fun NavGraphBuilder.fanCardScreen(
    terminate: () -> Unit,
) {
    composable(
        route = FanCardRoute,
        arguments = listOf(navArgument(FanCardId) { type = NavType.StringType }),
    ) {
        FanCardRoute(
            modifier = Modifier.fillMaxSize(),
            creatorId = FanboxCreatorId(it.arguments?.getString(FanCardId).orEmpty()),
            terminate = terminate,
        )
    }
}
